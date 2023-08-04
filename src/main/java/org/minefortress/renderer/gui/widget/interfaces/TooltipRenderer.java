package org.minefortress.renderer.gui.widget.interfaces;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@FunctionalInterface
public interface TooltipRenderer {
    void render(DrawContext matrices, Text text, int mouseX, int mouseY);
}