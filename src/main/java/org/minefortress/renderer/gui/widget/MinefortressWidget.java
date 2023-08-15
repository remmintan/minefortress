package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.item.ItemRenderer;

abstract class MinefortressWidget implements Element {

    protected  ItemRenderer getItemRenderer() {
        return MinecraftClient.getInstance().getItemRenderer();
    }

    protected TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

}
