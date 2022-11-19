package org.minefortress.renderer.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractHudLayer extends DrawableHelper {

    private final List<IHudButton> fortressHudButtons = new ArrayList<>();

    protected final MinecraftClient client;
    protected final ItemRenderer itemRenderer;
    protected final TextRenderer textRenderer;

    private int basepointX;
    private int basepointY;
    private boolean centeredX;
    private boolean centeredY;

    protected AbstractHudLayer(MinecraftClient client, ItemRenderer itemRenderer) {
        this.client = client;
        this.itemRenderer = itemRenderer;
        this.textRenderer = client.textRenderer;
    }

    protected final void setBasepoint(int x, int y, boolean centeredX, boolean centeredY) {
        this.basepointX = x;
        this.basepointY = y;
        this.centeredX = centeredX;
        this.centeredY = centeredY;
    }

    protected final void addButton(IHudButton button) {
        if(button instanceof IItemButton itemButton)
            itemButton.setItemRenderer(itemRenderer);
        fortressHudButtons.add(button);
    }

    void tick() {

    }
    final void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        final var baseX = centeredX ? screenWidth / 2 : screenWidth + basepointX;
        final var baseY = centeredY ? screenHeight / 2 : screenHeight + basepointY;
        for (IHudButton fortressHudButton : fortressHudButtons) {
            fortressHudButton.setPos(basepointX, basepointY);
        }
    }
    abstract boolean isHovered();
    void onClick(double mouseX, double mouseY) {}

    public void renderTooltip(MatrixStack matrices, Text text, int x, int y) {
        this.renderOrderedTooltip(matrices, List.of(text.asOrderedText()), x, y);
    }

    public void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y) {
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
            k = tooltipComponent.getWidth(this.textRenderer);
            if (k > i) {
                i = k;
            }
            j += tooltipComponent.getHeight();
        }
        int l = x + 12;
        int tooltipComponent = y - 12;
        k = i;
        int m = j;
        if (l + i > this.client.getWindow().getScaledWidth()) {
            l -= 28 + i;
        }
        if (tooltipComponent + m + 6 > this.client.getWindow().getScaledHeight()) {
            tooltipComponent = this.client.getWindow().getScaledHeight() - m - 6;
        }
        matrices.push();
        int n = -267386864;
        int o = 0x505000FF;
        int p = 1344798847;
        int q = 400;
        float f = this.itemRenderer.zOffset;
        this.itemRenderer.zOffset = 400.0f;
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
            tooltipComponent2.drawText(this.textRenderer, l, r, matrix4f, immediate);
            r += tooltipComponent2.getHeight() + (s == 0 ? 2 : 0);
        }
        immediate.draw();
        matrices.pop();
        r = tooltipComponent;
        for (s = 0; s < components.size(); ++s) {
            tooltipComponent2 = components.get(s);
            tooltipComponent2.drawItems(this.textRenderer, l, r, matrices, this.itemRenderer, 400);
            r += tooltipComponent2.getHeight() + (s == 0 ? 2 : 0);
        }
        this.itemRenderer.zOffset = f;
    }
}
