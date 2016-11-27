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

package pl.asie.charset.audio;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import pl.asie.charset.audio.tape.ItemTape;

public class ProxyClient extends ProxyCommon {
	@Override
	public void init() {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemTape.Color(ModCharsetAudio.tapeItem), ModCharsetAudio.tapeItem);
	}
}
