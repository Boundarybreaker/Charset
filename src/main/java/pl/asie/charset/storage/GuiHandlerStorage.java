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

package pl.asie.charset.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import pl.asie.charset.storage.backpack.ContainerBackpack;
import pl.asie.charset.storage.backpack.GuiBackpack;
import pl.asie.charset.storage.backpack.ItemBackpack;

public class GuiHandlerStorage implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case 2:
				ItemStack backpack = ItemBackpack.getBackpack(player);
				if (backpack != null) {
					IInventory inv = ((ItemBackpack) backpack.getItem()).getInventory(backpack);
					return new ContainerBackpack(inv, player.inventory);
				}
				return null;
		}

		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if (tile instanceof IInteractionObject) {
			Container container = ((IInteractionObject) tile).createContainer(player.inventory, player);
			switch (id) {
				case 1:
					return container instanceof ContainerBackpack ? container : null;
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		Container container = (Container) getServerGuiElement(id, player, world, x, y, z);

		switch (id) {
			case 1:
			case 2:
				return new GuiBackpack(container);
		}
		return null;
	}
}
