/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.module.transport.color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.resources.CharsetFakeResourcePack;
import pl.asie.charset.lib.utils.ColorUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

@CharsetModule(
		name = "transport.dyeableMinecarts",
		description = "Use dyes on Minecarts to make them colorful"
)
public class CharsetTransportDyeableMinecarts {
	public static class CapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
		private final MinecartDyeable dyeable = new MinecartDyeable();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == MINECART_DYEABLE;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == MINECART_DYEABLE ? (T) dyeable : null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return (NBTTagCompound) MINECART_DYEABLE.writeNBT(dyeable, null);
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			MINECART_DYEABLE.readNBT(dyeable, null, nbt);
		}
	}

	@CharsetModule.PacketRegistry("dyeMinecarts")
	public static PacketRegistry packet;

	@CapabilityInject(MinecartDyeable.class)
	public static Capability<MinecartDyeable> MINECART_DYEABLE;
	public static ResourceLocation MINECART_DYEABLE_KEY = new ResourceLocation("charsettweaks:minecart_dyeable");

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		CharsetFakeResourcePack.INSTANCE.registerEntry(ModelMinecartWrapped.DYEABLE_MINECART, (stream) -> {
			try {
				BufferedImage image = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(ModelMinecartWrapped.MINECART).getInputStream());
				float scale = (float) image.getWidth() / 64;
				int maxValue = 0;

				for (int y = 0; y < 28*scale; y++) {
					for (int x = 0; x < 44*scale; x++) {
						int rgb = image.getRGB(x, y);
						if (((rgb >> 24) & 0xFF) != 0) {
							for (int i = 0; i < 3; i++, rgb >>= 8) {
								if ((rgb & 0xFF) > maxValue) {
									maxValue = (rgb & 0xFF);
									if (maxValue == 255) break;
								}
							}
						}
						if (maxValue == 255) break;
					}
					if (maxValue == 255) break;
				}

				maxValue = maxValue * 8 / 9;

				if (maxValue < 255) {
					for (int y = 0; y < 28 * scale; y++) {
						for (int x = 0; x < 44 * scale; x++) {
							int rgb = image.getRGB(x, y);
							if (((rgb >> 24) & 0xFF) != 0) {
								for (int i = 0; i < 3; i++) {
									int mask = 0xFF << (i * 8);
									int v = (rgb >> (i * 8)) & 0xFF;
									v = v * 255 / maxValue;
									if (v > 255) v = 255;

									rgb = (rgb & (~mask)) | (v << (i * 8));
								}
								image.setRGB(x, y, rgb);
							}
						}
					}
				}

				ImageIO.write(image, "png", stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		packet.registerPacket(0x01, PacketMinecartUpdate.class);
		packet.registerPacket(0x02, PacketMinecartRequest.class);

		CapabilityManager.INSTANCE.register(MinecartDyeable.class, new MinecartDyeable.Storage(), MinecartDyeable.class);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void overrideRenderers(FMLPostInitializationEvent event) {
		Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap = Minecraft.getMinecraft().getRenderManager().entityRenderMap;

		for (Render<? extends Entity> e : entityRenderMap.values()) {
			if (e instanceof RenderMinecart) {
				Field f;

				try {
					f = RenderMinecart.class.getDeclaredField("modelMinecart");
				} catch (NoSuchFieldException eee) {
					try {
						f = RenderMinecart.class.getDeclaredField("field_77013_a");
					} catch (NoSuchFieldException ee) {
						f = null;
					}
				}

				if (f != null) {
					try {
						f.setAccessible(true);
						f.set(e, new ModelMinecartWrapped((ModelBase) f.get(e)));
					} catch (IllegalAccessException eee) {
						eee.printStackTrace();
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent event) {
		if (event.getObject() instanceof EntityMinecart) {
			event.addCapability(MINECART_DYEABLE_KEY, new CapabilityProvider());
		}
	}

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent event) {
		if (event.getEntity().world.isRemote && event.getEntity() instanceof EntityMinecart) {
			PacketMinecartRequest.send((EntityMinecart) event.getEntity());
		}
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		 if (event.getTarget() instanceof EntityMinecart
				 && !event.getTarget().getEntityWorld().isRemote
				 && event.getEntityPlayer().isSneaking()
				 && ColorUtils.isDye(event.getEntityPlayer().getHeldItem(event.getHand()))) {
			MinecartDyeable properties = MinecartDyeable.get((EntityMinecart) event.getTarget());
			if (properties != null) {
				properties.setColor(ColorUtils.getDyeColor(event.getEntityPlayer().getHeldItem(event.getHand())));

				event.setCanceled(true);
				event.getEntityPlayer().swingArm(event.getHand());

				PacketMinecartUpdate.send((EntityMinecart) event.getTarget());
			}
		}
	}
}
