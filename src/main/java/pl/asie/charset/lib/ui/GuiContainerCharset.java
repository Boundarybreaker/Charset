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

package pl.asie.charset.lib.ui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public class GuiContainerCharset extends GuiContainer {
	protected int xCenter, yCenter;

	public GuiContainerCharset(Container container, int xSize, int ySize) {
		super(container);
		this.xSize = xSize;
		this.ySize = ySize;
	}

	protected final boolean insideRect(int x, int y, int x0, int y0, int w, int h) {
		return x >= x0 && y >= y0 && x < (x0 + w) && y < (y0 + h);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.xCenter = (this.width - this.xSize) / 2;
		this.yCenter = (this.height - this.ySize) / 2;
	}
}
