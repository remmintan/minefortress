package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

abstract class FortressGuiScreen extends DrawableHelper {



    protected final MinecraftClient client;
    protected final ItemRenderer itemRenderer;

    protected FortressGuiScreen(MinecraftClient client, ItemRenderer itemRenderer) {
        this.client = client;
        this.itemRenderer = itemRenderer;
    }

    abstract void tick();
    abstract void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta);
    abstract boolean isHovered();
}
