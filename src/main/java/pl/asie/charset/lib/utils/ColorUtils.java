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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.Map;

public final class ColorUtils {
	private static final String[] UNDERSCORE_DYE_SUFFIXES = new String[]{
			"white", "orange", "magenta", "light_blue",
			"yellow", "lime", "pink", "gray",
			"silver", "cyan", "purple", "blue",
			"brown", "green", "red", "black"
	};

	private static final String[] UPPERCASE_DYE_SUFFIXES = new String[]{
			"White", "Orange", "Magenta", "LightBlue",
			"Yellow", "Lime", "Pink", "Gray",
			"LightGray", "Cyan", "Purple", "Blue",
			"Brown", "Green", "Red", "Black"
	};

	private static final String[] LOWERCASE_DYE_SUFFIXES = new String[]{
			"white", "orange", "magenta", "lightBlue",
			"yellow", "lime", "pink", "gray",
			"lightGray", "cyan", "purple", "blue",
			"brown", "green", "red", "black"
	};

	private static TIntObjectMap<EnumDyeColor> oredictDyeIdMap;

	private static final char[] WOOL_TO_CHAT = new char[]{
			'f', '6', 'd', '9', 'e', 'a', 'd', '8',
			'7', '3', '5', '1', '6', '2', '4', '0'
	};

	private static final Map<EnumDyeColor, float[]> DYE_TO_RGB;

	static {
		MethodHandle DYE_TO_RGB_GETTER = MethodHandleHelper.findFieldGetter(EntitySheep.class, "DYE_TO_RGB", "field_175514_bm");
		Map<EnumDyeColor, float[]> map = null;
		try {
			map = (Map<EnumDyeColor, float[]>) DYE_TO_RGB_GETTER.invokeExact();
		} catch (Throwable t) {

		}
		DYE_TO_RGB = map;
	}

	public static float[] getDyeRgb(EnumDyeColor dyeColor) {
		return DYE_TO_RGB.get(dyeColor);
	}

	private ColorUtils() {

	}

	public static TIntObjectMap<EnumDyeColor> getOredictDyeIdMap() {
		if (oredictDyeIdMap == null) {
		    oredictDyeIdMap = new TIntObjectHashMap<>();
			for (int i = 0; i < 16; i++) {
				oredictDyeIdMap.put(OreDictionary.getOreID("dye" + UPPERCASE_DYE_SUFFIXES[i]), EnumDyeColor.byMetadata(i));
			}
		}

		return oredictDyeIdMap;
	}

	public static String getNearestTextFormatting(EnumDyeColor color) {
		return "\u00a7" + WOOL_TO_CHAT[color.getMetadata()];
	}

	public static @Nullable EnumDyeColor getDyeColor(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}

		if (stack.getItem() == Items.DYE) {
			return EnumDyeColor.byDyeDamage(stack.getItemDamage());
		}

		int[] itemOreIDs = OreDictionary.getOreIDs(stack);
		TIntObjectMap<EnumDyeColor> map = getOredictDyeIdMap();
		for (int id : itemOreIDs) {
			EnumDyeColor color = map.get(id);
			if (color != null)
				return color;
		}

		return null;
	}

	public static boolean isDye(ItemStack stack) {
		return getDyeColor(stack) != null;
	}

	public static int toIntColor(EnumDyeColor color) {
		float[] d = ColorUtils.getDyeRgb(color);
		return    (Math.min(Math.round(d[0] * 255.0F), 255) << 16)
				| (Math.min(Math.round(d[1] * 255.0F), 255) << 8)
				| (Math.min(Math.round(d[2] * 255.0F), 255))
				| 0xFF000000;
	}

	public static String getOreDictEntry(String prefix, EnumDyeColor wool) {
		return prefix + UPPERCASE_DYE_SUFFIXES[wool.getMetadata()];
	}

	public static String getLangEntry(String prefix, EnumDyeColor wool) {
		return prefix + LOWERCASE_DYE_SUFFIXES[wool.getMetadata()];
	}

	// TODO: Move to ChatUtils?
	public static String stripChatColor(String chat) {
		return chat.replaceAll("[§][0-9A-FK-ORa-fk-or]", "");
	}

	public static String getUnderscoredSuffix(EnumDyeColor color) {
		return UNDERSCORE_DYE_SUFFIXES[color.getMetadata()];
	}

	public static float[] getYIQ(int c1) {
		float r1 = ((c1 >> 16) & 0xFF) / 255.0f;
		float g1 = ((c1 >> 8) & 0xFF) / 255.0f;
		float b1 = (c1 & 0xFF) / 255.0f;

		// YUV
		/* float y1 = 0.299f * r1 + 0.587f * g1 + 0.114f * b1;
		float u1 = -0.147f * r1 - 0.289f * g1 + 0.436f * b1;
		float v1 = 0.615f * r1 - 0.515f * g1 - 0.100f * b1; */

		// YIQ
		float y1 = 0.299f * r1 + 0.587f * g1 + 0.114f * b1;
		float u1 = 0.596f * r1 - 0.274f * g1 - 0.322f * b1;
		float v1 = 0.211f * r1 - 0.523f * g1 + 0.312f * b1;

		// weighted RGB
		/* float y1 = (float) Math.sqrt(2 + (r1 / 255)) * r1;
		float u1 = 2 * g1;
		float v1 = (float) Math.sqrt(3 - (r1 / 255)) * b1; */

		return new float[]{y1, u1, v1};
	}

	public static double getColorDistance(float[] c1, float[] c2) {
		return Math.sqrt(getColorDistanceSq(c1, c2));
	}

	public static double getColorDistance(int c1, int c2) {
		return Math.sqrt(getColorDistanceSq(c1, c2));
	}

	public static double getColorDistanceSq(float[] f1, float[] f2) {
		return (f1[0] - f2[0]) * (f1[0] - f2[0]) +
				(f1[1] - f2[1]) * (f1[1] - f2[1]) +
				(f1[2] - f2[2]) * (f1[2] - f2[2]);
	}

	public static double getColorDistanceSq(int c1, int c2) {
		float[] f1 = getYIQ(c1);
		float[] f2 = getYIQ(c2);

		return (f1[0] - f2[0]) * (f1[0] - f2[0]) +
				(f1[1] - f2[1]) * (f1[1] - f2[1]) +
				(f1[2] - f2[2]) * (f1[2] - f2[2]);
	}
}
