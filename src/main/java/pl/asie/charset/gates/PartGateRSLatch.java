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

package pl.asie.charset.gates;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

public class PartGateRSLatch extends PartGate {
	private boolean toggled;
	private boolean burnt;

	@Override
	protected boolean tick() {
		boolean oldIS = getValueInside(EnumFacing.WEST) != 0;
		boolean oldIR = getValueInside(EnumFacing.EAST) != 0;

		super.tick();

		boolean newIS = getValueInside(EnumFacing.WEST) != 0;
		boolean newIR = getValueInside(EnumFacing.EAST) != 0;

		int state = ((oldIR != newIR && newIR) ? 1 : 0) | ((oldIS != newIS && newIS) ? 2 : 0);

		switch (state) {
			case 0:
			default:
				return false;
			case 1:
				if (toggled) {
					toggled = false;
					return true;
				}
				return false;
			case 2:
				if (!toggled) {
					toggled = true;
					return true;
				}
				return false;
			case 3:
				burnt = true;
				BlockPos pos = getPos();
				getWorld().playSound(pos.getX() + 0.5F, pos.getY() + 0.1F, pos.getZ() + 0.5F,
						new SoundEvent(new ResourceLocation("random.fizz")), SoundCategory.BLOCKS, 0.5F, 2.6F + (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.8F, true);
				return true;
		}
	}

	@Override
	public void handlePacket(ByteBuf buf) {
		super.handlePacket(buf);
		burnt = buf.readBoolean();
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		buf.writeBoolean(burnt);
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return false;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir.getAxis() == EnumFacing.Axis.X ? Connection.INPUT_OUTPUT : Connection.OUTPUT;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setBoolean("tg", toggled);
		tag.setBoolean("br", burnt);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		toggled = tag.getBoolean("tg");
		burnt = tag.getBoolean("br");
		super.readFromNBT(tag);
	}

	@Override
	public State getLayerState(int id) {
		if (burnt) {
			return State.OFF;
		}
		switch (id) {
			case 1:
				return State.input(getValueInside(EnumFacing.NORTH));
			case 0:
				return State.input(getValueInside(EnumFacing.SOUTH));
		}
		return null;
	}

	@Override
	public State getTorchState(int id) {
		if (burnt) {
			return State.OFF;
		}
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.NORTH));
			case 1:
				return State.input(getValueInside(EnumFacing.SOUTH));
		}
		return null;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		if (burnt) {
			return 0;
		}
		return (toggled ^ (facing == EnumFacing.NORTH || facing == EnumFacing.EAST)) ? (byte) 15 : 0;
	}
}
