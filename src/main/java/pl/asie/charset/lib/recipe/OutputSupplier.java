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

package pl.asie.charset.lib.recipe;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import pl.asie.charset.ModCharset;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.*;
import java.util.Map;

public class OutputSupplier {
    private static class Stack implements IOutputSupplier {
        private final ItemStack stack;

        Stack(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
            return stack;
        }

        @Override
        public ItemStack getDefaultOutput() {
            return stack;
        }
    }

    private static final Gson GSON = new Gson();
    private static final Map<String, IOutputSupplierFactory> outputSuppliers = Maps.newHashMap();
    private static boolean initialized;

    // TODO: Allow iterating over mod assets server-side generically (based on this code, no less - but also UCW!)
    private static void loadFactories(ModContainer container) {
        File file = container.getSource();
        try {
            BufferedReader reader;
            if (file.exists()) {
                if (file.isDirectory()) {
                    File f = new File(file, "assets/" + container.getModId() + "/recipes/_factories.json");
                    if (f.exists()) {
                        reader = Files.newBufferedReader(f.toPath());
                    } else {
                        return;
                    }
                } else {
                    FileSystem fileSystem = FileSystems.newFileSystem(file.toPath(), null);
                    Path p = fileSystem.getPath("assets/" + container.getModId() + "/recipes/_factories.json");
                    reader = Files.newBufferedReader(p, Charsets.UTF_8);
                }
            } else {
                return;
            }
            JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
            if (json != null && json.has("charset:output_suppliers")) {
                JsonObject object = JsonUtils.getJsonObject(json, "charset:output_suppliers");
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    String key = new ResourceLocation(container.getModId(), entry.getKey()).toString();
                    String value = entry.getValue().getAsString();
                    try {
                        Object o = Class.forName(value).newInstance();
                        if (o instanceof IOutputSupplierFactory) {
                            outputSuppliers.put(key, (IOutputSupplierFactory) o);
                        } else if (o instanceof IOutputSupplier) {
                            outputSuppliers.put(key, (a, b) -> (IOutputSupplier) o);
                        } else {
                            throw new Exception("Invalid OutputSupplier object type: " + (o != null ? o.getClass().getName() : "null"));
                        }
                    } catch (Exception e) {
                        ModCharset.logger.warn("Could not create IOutputSupplierFactory " + value + ": " + e.getMessage());
                    }
                }
            }
        } catch (NoSuchFileException | FileSystemNotFoundException e) {
            // Don't worry~
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initialize() {
        if (initialized) return;

        Loader.instance().getActiveModList().forEach(OutputSupplier::loadFactories);

        initialized = true;
    }

    public static IOutputSupplier createOutputSupplier(JsonContext context, JsonObject json) {
        initialize();
        if (json.has("supplier")) {
            return outputSuppliers.get(JsonUtils.getString(json, "supplier")).parse(context, (JsonObject) json);
        } else {
            return new Stack(CraftingHelper.getItemStack(json, context));
        }
    }

    public static IOutputSupplier createStackOutputSupplier(ItemStack stack) {
        return new Stack(stack);
    }
}
