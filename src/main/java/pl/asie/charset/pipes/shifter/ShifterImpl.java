/*
 * Copyright (c) 2015-2016 Adrian Siekierka, rubensworks
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

package pl.asie.charset.pipes.shifter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.utils.FluidUtils;
import pl.asie.charset.lib.utils.ItemUtils;

/**
 * Default implementation for the shifter.
 * @author rubensworks
 */
public class ShifterImpl implements IShifter {
    private Mode mode;
    private EnumFacing direction;
    private int shiftDistance;
    private boolean shifting;
    private boolean hasFilter;
    private ItemStack filter;

    public ShifterImpl(Mode mode, EnumFacing direction, int shiftDistance, boolean isShifting,
                       boolean hasFilter, ItemStack filter) {
        this.mode = mode;
        this.direction = direction;
        this.shiftDistance = shiftDistance;
        this.shifting = isShifting;
        this.hasFilter = hasFilter;
        this.filter = filter;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public EnumFacing getDirection() {
        return direction;
    }

    @Override
    public int getShiftDistance() {
        return shiftDistance;
    }

    @Override
    public boolean isShifting() {
        return shifting;
    }

    @Override
    public boolean hasFilter() {
        return hasFilter;
    }

    public ItemStack getFilter() {
        return filter;
    }

    @Override
    public boolean matches(ItemStack source) {
        return filter.isEmpty() || ItemUtils.equals(filter, source, false, true, true);
    }

    @Override
    public boolean matches(FluidStack source) {
        if (source != null) {
            if (filter.isEmpty()) {
                return true;
            } else {
                IFluidHandler handler = CapabilityHelper.get(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, filter, null);
                return handler != null ? FluidUtils.matches(handler, source) : true;
            }
        } else {
            return false;
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setDirection(EnumFacing direction) {
        this.direction = direction;
    }

    public void setShiftDistance(int shiftDistance) {
        this.shiftDistance = shiftDistance;
    }

    public void setShifting(boolean shifting) {
        this.shifting = shifting;
    }

    public void setHasFilter(boolean hasFilter) {
        this.hasFilter = hasFilter;
    }

    public void setFilter(ItemStack filter) {
        this.filter = filter;
    }
}
