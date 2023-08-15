package org.minefortress.renderer.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;

import java.util.function.Supplier;

public class ModeButtonWidget extends ItemButtonWidget {

    private final Supplier<Boolean> isActiveSupplier;

    public ModeButtonWidget(
            int anchorX, int anchorY, Item item, PressAction clickAction, String tooltipText, Supplier<Boolean> isActiveSupplier
    ) {
        super(anchorX, anchorY, item, clickAction, tooltipText);
        this.isActiveSupplier = isActiveSupplier;
    }

    @Override
    public void renderButton(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        int v = 0;
        if (this.isSelected() || isActiveSupplier.get()) {
            int hoveredVOffset = 20;
            v += hoveredVOffset;
        }
        RenderSystem.enableDepthTest();
        int textureWidth = 32;
        int textureHeight = 64;
        int u = 0;
        this.drawTexture(drawContext, FORTRESS_BUTTON_TEXTURE, this.getX(), this.getY(), u, v, 20, this.width, this.height, textureWidth, textureHeight);
        if (this.hovered) {
            this.drawMessage(drawContext, MinecraftClient.getInstance().textRenderer, 0xFFFFFF);
        }
    }
}
