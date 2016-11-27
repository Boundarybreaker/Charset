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

package pl.asie.charset.api.pipes;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

/**
 * This interface is implemented by Charset's pipes.
 * Please do not implement it yourself.
 */
public interface IPipe {
	/**
	 * Get the stack closest to the middle of a given side of the pipe.
	 * WARNING: This is not a free function and should be used primarily
	 * by item detectors.
	 *
	 * @param side The side (null - center)
	 * @return The closest stack found.
	 */
	@Nullable ItemStack getTravellingStack(@Nullable EnumFacing side);
}
