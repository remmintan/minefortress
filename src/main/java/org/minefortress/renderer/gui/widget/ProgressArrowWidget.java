package org.minefortress.renderer.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class ProgressArrowWidget implements Drawable, Element {

    private static final Identifier PROGRESS_TEXTURE = new Identifier("textures/gui/container/blast_furnace.png");

    private final Supplier<Integer> progressSupplier;
    private final int x;
    private final int y;

    public ProgressArrowWidget(int x, int y, Supplier<Integer> progressSupplier) {
        this.x = x;
        this.y = y;
        this.progressSupplier = progressSupplier;
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, PROGRESS_TEXTURE);

        DrawContext.drawTexture(matrices, x-11, y+2, 80, 35, 22, 15, 256, 256);
        int rawProgress = progressSupplier.get();
        int progress = (int) (rawProgress * 0.22);
        if (progress > 0) {
            DrawContext.drawTexture(matrices, x-11, y+2, 177, 14, progress + 1, 16, 256, 256);
        }
    }
}
