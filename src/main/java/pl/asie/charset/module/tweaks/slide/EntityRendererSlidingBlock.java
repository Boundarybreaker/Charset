package pl.asie.charset.module.tweaks.slide;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import pl.asie.charset.ModCharset;
import pl.asie.charset.module.tweaks.carry.CarryHandler;

import javax.annotation.Nullable;
import java.util.HashSet;

public class EntityRendererSlidingBlock extends Render<EntitySlidingBlock> {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final HashSet<Class> caughtExceptionTileRenderers = new HashSet<>();

	protected EntityRendererSlidingBlock(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntitySlidingBlock entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x - 0.5f, y, z - 0.5f);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();

		try {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			IBlockState state = entity.block;

			if (state != null && state.getRenderType() == EnumBlockRenderType.MODEL) {
				buffer.setTranslation(0, -64, 0);
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

				try {
					IBlockState renderState = state.getActualState(entity.access, CarryHandler.ACCESS_POS);
					IBlockState renderStateExt = state.getBlock().getExtendedState(renderState, entity.access, CarryHandler.ACCESS_POS);

					BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
					IBakedModel model = brd.getModelForState(renderState);
					brd.getBlockModelRenderer().renderModelFlat(entity.access,
							model, renderStateExt,
							CarryHandler.ACCESS_POS, buffer, false, 0L
					);
				} catch (Exception e) {
					e.printStackTrace();
				}

				tessellator.draw();
				buffer.setTranslation(0, 0, 0);
			}

			TileEntity tile = entity.access.getTileEntity(CarryHandler.ACCESS_POS);
			if (tile != null) {
				RenderHelper.enableStandardItemLighting();
				int i = entity.access.getCombinedLight(CarryHandler.ACCESS_POS, 0);
				int j = i % 65536;
				int k = i / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

				try {
					TileEntityRendererDispatcher.instance.render(tile, 0, 0, 0, partialTicks);
				} catch (Exception e) {
					if (!caughtExceptionTileRenderers.contains(tile.getClass())) {
						e.printStackTrace();
						ModCharset.logger.warn("Future exceptions from this tile entity will be hidden.");
						caughtExceptionTileRenderers.add(tile.getClass());
					}
					// Hack of the Year award for the Least Graceful Recovery
					buffer.setTranslation(0, 0, 0);
					boolean caught = false;
					while (!caught) {
						try {
							tessellator.draw();
						} catch (IllegalStateException ee) {
							caught = true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		GlStateManager.popMatrix();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntitySlidingBlock entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
}