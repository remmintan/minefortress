package org.minefortress.renderer.gui.professions;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.minefortress.professions.ClientProfessionManager;
import org.minefortress.professions.Profession;

import java.util.ArrayList;
import java.util.List;

public class ProfessionWidget extends DrawableHelper {

    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");

    private ProfessionWidget parent;
    private final List<ProfessionWidget> children = new ArrayList<>();
    private final Profession profession;

    private int x = 0;
    private int y = 0;

    public ProfessionWidget(Profession profession) {
        this.profession = profession;
    }

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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        final int v = 128 + ClientProfessionManager.getStatus(profession).getSpriteIndex() * 26;
        final int u = profession.getType().getTextureV();
        this.drawTexture(matrices, x + this.x + 3, y + this.y, u, v, 26, 26);

        getItemRenderer().renderInGui(profession.getIcon(), x + this.x + 8, y + this.y + 5);
        getTextRenderer().draw(matrices, ""+profession.getAmount(), x + this.x + 8, y + this.y + 5, 0xFFFFFF);

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
