/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.utils;

import com.google.common.base.Charsets;
import net.minecraft.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public final class AttributeUtils {
	public enum Operation {
		ADD,
		ADD_MULTIPLIED,
		MULTIPLY_PLUS_ONE
	}

	private AttributeUtils() {

	}

	public static AttributeModifier newModifier(String name, double amount, Operation operation) {
		return new AttributeModifier(name, amount, operation.ordinal());
	}

	public static AttributeModifier newModifierSingleton(String name, double amount, Operation operation) {
		return new AttributeModifier(UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8)), name, amount, operation.ordinal());
	}
}
