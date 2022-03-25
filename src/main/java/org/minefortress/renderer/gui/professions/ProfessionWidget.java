package org.minefortress.renderer.gui.professions;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ProfessionWidget extends DrawableHelper {

    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");

    private ProfessionWidget parent;
    private final List<ProfessionWidget> children = new ArrayList<>();

    private int x = 0;
    private int y = 0;

    public void renderLines(MatrixStack matrices, int x, int y, boolean bl) {
        if (this.parent != null) {
            int i = x + this.parent.x + 13;
            int j = x + this.parent.x + 26 + 4;
            int k = y + this.parent.y + 13;
            int l = x + this.x + 13;
            int m = y + this.y + 13;
            int n = bl ? -16777216 : -1;
            if (bl) {
                this.drawHorizontalLine(matrices, j, i, k - 1, n);
                this.drawHorizontalLine(matrices, j + 1, i, k, n);
                this.drawHorizontalLine(matrices, j, i, k + 1, n);
                this.drawHorizontalLine(matrices, l, j - 1, m - 1, n);
                this.drawHorizontalLine(matrices, l, j - 1, m, n);
                this.drawHorizontalLine(matrices, l, j - 1, m + 1, n);
                this.drawVerticalLine(matrices, j - 1, m, k, n);
                this.drawVerticalLine(matrices, j + 1, m, k, n);
            } else {
                this.drawHorizontalLine(matrices, j, i, k, n);
                this.drawHorizontalLine(matrices, l, j, m, n);
                this.drawVerticalLine(matrices, j, m, k, n);
            }
        }
        for (ProfessionWidget child : this.children) {
            child.renderLines(matrices, x, y, bl);
        }
    }

    public void renderWidgets(MatrixStack matrices, int x, int y){
        AdvancementObtainedStatus status = AdvancementObtainedStatus.OBTAINED;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        this.drawTexture(matrices, x + this.x + 3, y + this.y, AdvancementFrame.TASK.getTextureV(), 128 + status.getSpriteIndex() * 26, 26, 26);

        getItemRenderer().renderInGui(new ItemStack(Items.WOODEN_AXE), x + this.x + 8, y + this.y + 5);
        getTextRenderer().draw(matrices, "0", x + this.x + 8, y + this.y + 5, 0xFFFFFF);

        for (ProfessionWidget advancementWidget : this.children) {
            advancementWidget.renderWidgets(matrices, x, y);
        }
    }

    private ItemRenderer getItemRenderer() {
        return getInstance().getItemRenderer();
    }

    private TextRenderer getTextRenderer() {
        return getInstance().textRenderer;
    }

    private MinecraftClient getInstance() {
        return MinecraftClient.getInstance();
    }

    public void setParent(ProfessionWidget parent) {
        this.parent = parent;
    }

    public void addChild(ProfessionWidget child) {
        children.add(child);
    }

    List<ProfessionWidget> getChildren() {
        return children;
    }

    void setPos(int column, float row){
        this.x = MathHelper.floor(column * 28.0f);
        this.y = MathHelper.floor(row * 27.0f);
    }

}
