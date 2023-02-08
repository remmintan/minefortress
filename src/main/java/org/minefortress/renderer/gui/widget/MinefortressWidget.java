package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;

class MinefortressWidget {

    protected  ItemRenderer getItemRenderer() {
        return MinecraftClient.getInstance().getItemRenderer();
    }

    protected TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

}
