package org.minefortress.renderer.gui.professions;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.minefortress.professions.ProfessionManager;
import org.minefortress.professions.Profession;

import java.util.ArrayList;
import java.util.List;

public class ProfessionWidget extends DrawableHelper {

    public static final float PROFESSION_WIDGET_WIDTH = 64f;
    public static final float PROFESSION_WIDGET_HEIGHT = 35f;
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");

    private ProfessionWidget parent;
    private final List<ProfessionWidget> children = new ArrayList<>();
    private final Profession profession;
    private final ProfessionManager professionManager;

    private int x = 0;
    private int y = 0;

    private final int width;
    private final MinecraftClient client;

    public ProfessionWidget(Profession profession, ProfessionManager professionManager) {
        this.profession = profession;
        this.professionManager = professionManager;
        client = MinecraftClient.getInstance();
        int maxTextLength = 29 + client.textRenderer.getWidth(profession.getTitle());
        for(Text text : this.profession.getUnlockedDescription()) {
            maxTextLength = Math.max(maxTextLength, client.textRenderer.getWidth(text));
        }
        this.width = maxTextLength + 8;
    }

    public void renderLines(MatrixStack matrices, int x, int y, boolean bl) {
        if (this.parent != null) {
            int i = x + this.parent.x + 13;
            int j = x + this.parent.x + 26 + 22;
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
        if(!isUnlocked()) return;
        for (ProfessionWidget child : this.children) {
            child.renderLines(matrices, x, y, bl);
        }
    }

    public void renderWidgets(MatrixStack matrices, int x, int y){
        final boolean unlocked = isUnlocked();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        AdvancementObtainedStatus status = unlocked ? AdvancementObtainedStatus.OBTAINED : AdvancementObtainedStatus.UNOBTAINED;
        final int v = 128 + status.getSpriteIndex() * 26;
        final int u = profession.getType().getTextureV();
        this.drawTexture(matrices, x + this.x + 3, y + this.y, u, v, 26, 26);

        getItemRenderer().renderInGui(profession.getIcon(), x + this.x + 8, y + this.y + 5);
        matrices.push();
        matrices.translate(0.0, 0.0, 200.0);
        getTextRenderer().draw(matrices, ""+profession.getAmount(), x + this.x + 6, y + this.y + 4, 0xFFFFFF);
        matrices.pop();
        final String title = profession.getTitle().contains("-") ? profession.getTitle().split("-")[0] : profession.getTitle();
        final String trimmedTitle = getTextRenderer().trimToWidth(title, (int) (PROFESSION_WIDGET_WIDTH - 4));
        final int titleWidth = getTextRenderer().getWidth(trimmedTitle);
        getTextRenderer().draw(matrices, trimmedTitle, x + this.x + 4f  - titleWidth/2f + 13f  , y + this.y + 27, 0xFFFFFF);

        if(!unlocked) return;
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

    void setParent(ProfessionWidget parent) {
        this.parent = parent;
    }

    void addChild(ProfessionWidget child) {
        children.add(child);
    }

    List<ProfessionWidget> getChildren() {
        return children;
    }

    void setPos(int column, float row){
        this.x = MathHelper.floor(column * PROFESSION_WIDGET_WIDTH);
        this.y = MathHelper.floor(row * PROFESSION_WIDGET_HEIGHT);
    }

    boolean shouldRender(int originX, int originY, int mouseX, int mouseY) {
        int i = originX + this.x;
        int j = i + 26;
        int k = originY + this.y;
        int l = k + 26;
        return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
    }

    public void drawTooltip(MatrixStack matrices, int originX, int originY, float alpha, int x, int y, int screenWidth) {
        final boolean unlocked = isUnlocked();
        AdvancementObtainedStatus status = unlocked?AdvancementObtainedStatus.OBTAINED:AdvancementObtainedStatus.UNOBTAINED;
        int j = MathHelper.floor((float)this.width);
        int k = this.width - j;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        int l = originY + this.y;
        boolean bl = x + originX + this.x + this.width + 26 >= screenWidth;
        int m = bl ? originX + this.x - this.width + 26 + 6 : originX + this.x;

        final String title = this.profession.getTitle();
        final List<Text> description = unlocked? this.profession.getUnlockedDescription(): this.profession.getLockedDescription();

        int n = 32 + description.size() * this.client.textRenderer.fontHeight;
        boolean bl2 = 113 - originY - this.y - 26 <= 6 + description.size() * client.textRenderer.fontHeight;
        if (!description.isEmpty()) {
            if (bl2) {
                this.method_2324(matrices, m, l + 26 - n, this.width, n, 10, 200, 26, 0, 52);
            } else {
                this.method_2324(matrices, m, l, this.width, n, 10, 200, 26, 0, 52);
            }
        }
        this.drawTexture(matrices, m, l, 0, status.getSpriteIndex() * 26, j, 26);
        this.drawTexture(matrices, m + j, l, 200 - k, status.getSpriteIndex() * 26, k, 26);
        this.drawTexture(matrices, originX + this.x + 3, originY + this.y, this.profession.getType().getTextureV(), 128 + status.getSpriteIndex() * 26, 26, 26);
        matrices.push();
        matrices.translate(0.0, 0.0, 200.0);
        getTextRenderer().draw(matrices, ""+profession.getAmount(), m + 6, originY + this.y + 4, 0xFFFFFFFF);
        matrices.pop();
        if (bl) {
            this.client.textRenderer.drawWithShadow(matrices, title, (float)(m + 5), (float)(originY + this.y + 9), 0xffffffff);
        } else {
            this.client.textRenderer.drawWithShadow(matrices, title, (float)(originX + this.x + 32), (float)(originY + this.y + 9), -1);
        }
        if (bl2) {
            for (int o = 0; o < description.size(); ++o) {
                this.client.textRenderer.draw(matrices, description.get(o), (float)(m + 5), (float)(l + 26 - n + 7 + o * this.client.textRenderer.fontHeight), -5592406);
            }
        } else {
            for (int o = 0; o < description.size(); ++o) {
                this.client.textRenderer.draw(matrices, description.get(o), (float)(m + 5), (float)(originY + this.y + 9 + 17 + o * this.client.textRenderer.fontHeight), -5592406);
            }
        }
        this.client.getItemRenderer().renderInGui(this.profession.getIcon(), originX + this.x + 8, originY + this.y + 5);
    }

    protected void method_2324(MatrixStack matrices, int x, int y, int i, int j, int k, int l, int m, int n, int o) {
        this.drawTexture(matrices, x, y, n, o, k, k);
        this.method_2321(matrices, x + k, y, i - k - k, k, n + k, o, l - k - k, m);
        this.drawTexture(matrices, x + i - k, y, n + l - k, o, k, k);
        this.drawTexture(matrices, x, y + j - k, n, o + m - k, k, k);
        this.method_2321(matrices, x + k, y + j - k, i - k - k, k, n + k, o + m - k, l - k - k, m);
        this.drawTexture(matrices, x + i - k, y + j - k, n + l - k, o + m - k, k, k);
        this.method_2321(matrices, x, y + k, k, j - k - k, n, o + k, l, m - k - k);
        this.method_2321(matrices, x + k, y + k, i - k - k, j - k - k, n + k, o + k, l - k - k, m - k - k);
        this.method_2321(matrices, x + i - k, y + k, k, j - k - k, n + l - k, o + k, l, m - k - k);
    }

    protected void method_2321(MatrixStack matrices, int x, int y, int i, int j, int k, int l, int m, int n) {
        for (int o = 0; o < i; o += m) {
            int p = x + o;
            int q = Math.min(m, i - o);
            for (int r = 0; r < j; r += n) {
                int s = y + r;
                int t = Math.min(n, j - r);
                this.drawTexture(matrices, p, s, k, l, q, t);
            }
        }
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    private boolean isUnlocked() {
        return this.professionManager.isRequirementsFulfilled(this.profession);
    }
}
