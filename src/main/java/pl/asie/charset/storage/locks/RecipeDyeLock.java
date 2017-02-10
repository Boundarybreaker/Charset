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

package pl.asie.charset.storage.locks;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import pl.asie.charset.lib.recipe.RecipeDyeableItem;

import java.util.Optional;

public class RecipeDyeLock extends RecipeDyeableItem {
    private int layer = 0;

    @Override
    protected boolean isTarget(ItemStack stack) {
        return stack.getItem() instanceof ItemKey || stack.getItem() instanceof ItemLock;
    }

    @Override
    protected int[] getColor(ItemStack stack) {
        if (stack != null && isTarget(stack)) {
            String key = "color" + layer;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey(key)) {
                int c = stack.getTagCompound().getInteger(key);
                return new int[] {(c >> 16) & 255, (c >> 8) & 255, c & 255};
            }
        }

        return super.getColor(stack);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack target = null;
        int dyeCount = 0;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack source = inv.getStackInSlot(i);
            if (!source.isEmpty()) {
                if (isTarget(source)) {
                    target = source.copy();
                    target.setCount(1);
                } else if (getColor(source) != null) {
                    dyeCount++;
                }
            }
        }

        layer = dyeCount >= 8 ? 0 : 1;

        if (target != null) {
            Optional<Integer> result = getMixedColor(inv, target, null);
            if (result.isPresent()) {
                String key = "color" + layer;
                if (!target.hasTagCompound()) {
                    target.setTagCompound(new NBTTagCompound());
                }
                target.getTagCompound().setInteger(key, result.get());
                return target;
            }
        }

        return null;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        // Nothing shall remain, for it is the key itself! We don't want to clone it.

        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }
}
