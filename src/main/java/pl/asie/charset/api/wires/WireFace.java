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

package pl.asie.charset.api.wires;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum WireFace {
	DOWN,
	UP,
	NORTH,
	SOUTH,
	WEST,
	EAST,
	CENTER;

	WireFace() {
		facing = ordinal() >= 6 ? null : EnumFacing.getFront(ordinal());
	}

	public static final WireFace[] VALUES = values();
	public final EnumFacing facing;

	/**
	 * Creates a WireFace from the given EnumFacing.
     */
	public static @Nonnull WireFace get(@Nullable EnumFacing facing) {
		return facing != null ? VALUES[facing.ordinal()] : CENTER;
	}
}
