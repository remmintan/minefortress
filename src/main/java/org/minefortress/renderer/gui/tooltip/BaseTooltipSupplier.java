package org.minefortress.renderer.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.Matrix4f;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

abstract class BaseTooltipSupplier extends DrawableHelper implements ButtonWidget.TooltipSupplier {

    @Nonnull
    protected abstract List<OrderedText> getTooltip();

    @Override
    public final void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        final var tooltip = getTooltip();
        if(tooltip.isEmpty()) return;
        this.renderOrderedTooltip(matrices, tooltip, mouseX, mouseY);
    }

    private void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y) {
        this.renderTooltipFromComponents(matrices, lines.stream().map(TooltipComponent::of).collect(Collectors.toList()), x, y);
    }

    private void renderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y) {
        TooltipComponent tooltipComponent2;
        int s;
        int k;
        if (components.isEmpty()) {
            return;
        }
        int i = 0;
        int j = components.size() == 1 ? -2 : 0;
        for (TooltipComponent tooltipComponent : components) {
            k = tooltipComponent.getWidth(this.getTextRenderer());
            if (k > i) {
                i = k;
            }
            j += tooltipComponent.getHeight();
        }
        int l = x + 12;
        int tooltipComponent = y - 12;
        k = i;
        int m = j;
        if (l + i > getClientWindow().getScaledWidth()) {
            l -= 28 + i;
        }
        if (tooltipComponent + m + 6 > getClientWindow().getScaledHeight()) {
            tooltipComponent = getClientWindow().getScaledHeight() - m - 6;
        }
        matrices.push();
        int n = -267386864;
        int o = 0x505000FF;
        int p = 1344798847;
        int q = 400;
        float f = this.getItemRenderer().zOffset;
        this.getItemRenderer().zOffset = 400.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Screen.fillGradient(matrix4f, bufferBuilder, l - 3, tooltipComponent - 4, l + k + 3, tooltipComponent - 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, l - 3, tooltipComponent + m + 3, l + k + 3, tooltipComponent + m + 4, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, l - 3, tooltipComponent - 3, l + k + 3, tooltipComponent + m + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, l - 4, tooltipComponent - 3, l - 3, tooltipComponent + m + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, l + k + 3, tooltipComponent - 3, l + k + 4, tooltipComponent + m + 3, 400, -267386864, -267386864);
        Screen.fillGradient(matrix4f, bufferBuilder, l - 3, tooltipComponent - 3 + 1, l - 3 + 1, tooltipComponent + m + 3 - 1, 400, 0x505000FF, 1344798847);
        Screen.fillGradient(matrix4f, bufferBuilder, l + k + 2, tooltipComponent - 3 + 1, l + k + 3, tooltipComponent + m + 3 - 1, 400, 0x505000FF, 1344798847);
        Screen.fillGradient(matrix4f, bufferBuilder, l - 3, tooltipComponent - 3, l + k + 3, tooltipComponent - 3 + 1, 400, 0x505000FF, 0x505000FF);
        Screen.fillGradient(matrix4f, bufferBuilder, l - 3, tooltipComponent + m + 2, l + k + 3, tooltipComponent + m + 3, 400, 1344798847, 1344798847);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        matrices.translate(0.0, 0.0, 400.0);
        int r = tooltipComponent;
        for (s = 0; s < components.size(); ++s) {
            tooltipComponent2 = components.get(s);
            tooltipComponent2.drawText(this.getTextRenderer(), l, r, matrix4f, immediate);
            r += tooltipComponent2.getHeight() + (s == 0 ? 2 : 0);
        }
        immediate.draw();
        matrices.pop();
        r = tooltipComponent;
        for (s = 0; s < components.size(); ++s) {
            tooltipComponent2 = components.get(s);
            tooltipComponent2.drawItems(this.getTextRenderer(), l, r, matrices, this.getItemRenderer(), 400);
            r += tooltipComponent2.getHeight() + (s == 0 ? 2 : 0);
        }
        this.getItemRenderer().zOffset = f;
    }

    private ItemRenderer getItemRenderer(){
        return MinecraftClient.getInstance().getItemRenderer();
    }

    private TextRenderer getTextRenderer(){
        return MinecraftClient.getInstance().textRenderer;
    }

    private Window getClientWindow(){
        return MinecraftClient.getInstance().getWindow();
    }

}
