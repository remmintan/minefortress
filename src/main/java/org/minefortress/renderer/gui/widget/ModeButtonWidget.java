package org.minefortress.renderer.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
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
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, ItemButtonWidget.FORTRESS_BUTTON_TEXTURE);
        int v = 0;
        if (this.isSelected() || isActiveSupplier.get()) {
            int hoveredVOffset = 20;
            v += hoveredVOffset;
        }
        RenderSystem.enableDepthTest();
        int textureWidth = 32;
        int textureHeight = 64;
        int u = 0;
        TexturedButtonWidget.drawTexture(matrices, this.x, this.y, u, v, this.width, this.height, textureWidth, textureHeight);
        if (this.hovered) {
            this.renderTooltip(matrices, mouseX, mouseY);
        }
    }
}
