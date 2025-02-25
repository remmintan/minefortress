package org.minefortress.renderer.gui.professions;


import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.remmintan.mods.minefortress.core.interfaces.professions.CountProfessionals;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfession;
import net.remmintan.mods.minefortress.core.interfaces.professions.ProfessionResearchState;
import net.remmintan.mods.minefortress.gui.util.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class ProfessionWidget {

    public static final float PROFESSION_WIDGET_WIDTH = 64f;
    public static final float PROFESSION_WIDGET_HEIGHT = 35f;
    private static final AdvancementFrame PROFESSIONS_FRAME = AdvancementFrame.TASK;
    private static final Identifier TITLE_BOX_TEXTURE = new Identifier("advancements/title_box");

    private ProfessionWidget parent;
    private final List<ProfessionWidget> children = new ArrayList<>();
    private final IProfession profession;
    private final IClientProfessionManager professionManager;

    private int x = 0;
    private int y = 0;

    private final int width;
    private final MinecraftClient client;

    public ProfessionWidget(IProfession profession, IClientProfessionManager professionManager) {
        this.profession = profession;
        this.professionManager = professionManager;
        client = MinecraftClient.getInstance();
        int maxTextLength = 29 + client.textRenderer.getWidth(profession.getTitle());
        for(Text text : this.profession.getDescription()) {
            maxTextLength = Math.max(maxTextLength, client.textRenderer.getWidth(text));
        }
        this.width = maxTextLength + 8;
    }

    public void renderLines(DrawContext drawContext, int x, int y, boolean bl) {
        if (this.parent != null) {
            int i = x + this.parent.x + 13;
            int j = x + this.parent.x + 26 + 22;
            int k = y + this.parent.y + 13;
            int l = x + this.x + 13;
            int m = y + this.y + 13;
            int n = bl ? -16777216 : -1;
            if (bl) {
                drawContext.drawHorizontalLine(j, i, k - 1, n);
                drawContext.drawHorizontalLine(j + 1, i, k, n);
                drawContext.drawHorizontalLine(j, i, k + 1, n);
                drawContext.drawHorizontalLine(l, j - 1, m - 1, n);
                drawContext.drawHorizontalLine(l, j - 1, m, n);
                drawContext.drawHorizontalLine(l, j - 1, m + 1, n);
                drawContext.drawVerticalLine(j - 1, m, k, n);
                drawContext.drawVerticalLine(j + 1, m, k, n);
            } else {
                drawContext.drawHorizontalLine(j, i, k, n);
                drawContext.drawHorizontalLine(l, j, m, n);
                drawContext.drawVerticalLine(j, m, k, n);
            }
        }
        for (ProfessionWidget child : this.children) {
            child.renderLines(drawContext, x, y, bl);
        }
    }

    public void renderWidgets(DrawContext drawContext, int x, int y){
        final boolean unlockedWithCount = isUnlocked() == ProfessionResearchState.UNLOCKED;

        AdvancementObtainedStatus status = unlockedWithCount ? AdvancementObtainedStatus.OBTAINED : AdvancementObtainedStatus.UNOBTAINED;
        drawContext.drawGuiTexture(status.getFrameTexture(PROFESSIONS_FRAME), x + this.x + 3, y + this.y, 26, 26);

        drawContext.drawItem(profession.getIcon(), x + this.x + 8, y + this.y + 5);
        final var matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(0.0, 0.0, 200.0);
        if (unlockedWithCount)
            drawContext.drawTextWithShadow(this.getTextRenderer(), String.valueOf(getAmount()), x + this.x + 6, y + this.y + 4, 0xFFFFFF);
        matrices.pop();
        final String title = profession.getTitle().contains("-") ? profession.getTitle().split("-")[0] : profession.getTitle();
        final String trimmedTitle = getTextRenderer().trimToWidth(title, (int) (PROFESSION_WIDGET_WIDTH - 4));
        final int titleWidth = getTextRenderer().getWidth(trimmedTitle);
        drawContext.drawTextWithShadow(this.getTextRenderer(), trimmedTitle, x + this.x + 4  - titleWidth/2 + 13, y + this.y + 27, 0xFFFFFF);

        for (ProfessionWidget professionWidget : this.children) {
            professionWidget.renderWidgets(drawContext, x, y);
        }
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

    boolean shouldNotRender(int originX, int originY, int mouseX, int mouseY) {
        int i = originX + this.x;
        int j = i + 26;
        int k = originY + this.y;
        int l = k + 26;
        return mouseX < i || mouseX > j || mouseY < k || mouseY > l;
    }

    public void drawTooltip(DrawContext drawContext, int originX, int originY, int x, int screenWidth) {
        final var unlocked = isUnlocked() == ProfessionResearchState.UNLOCKED;

        final boolean parentUnlocked = isUnlocked() != ProfessionResearchState.LOCKED_PARENT;

        AdvancementObtainedStatus status = unlocked ? AdvancementObtainedStatus.OBTAINED : AdvancementObtainedStatus.UNOBTAINED;
        int j = MathHelper.floor((float)this.width);
        int k = this.width - j;
        int l = originY + this.y;
        boolean bl = x + originX + this.x + this.width + 26 >= screenWidth;
        int m = bl ? originX + this.x - this.width + 26 + 6 : originX + this.x;

        final String title = this.profession.getTitle();
        List<Text> description;
        if (unlocked) {
            description = this.profession.getDescription();
        } else if (!parentUnlocked) {
            description = GuiUtils.splitTextInWordsForLength("You need to unlock the parent profession first!");
        } else {
            description = this.profession.getUnlockMessage();
        }

        int n = 32 + description.size() * this.client.textRenderer.fontHeight;
        boolean bl2 = 113 - originY - this.y - 26 <= 6 + description.size() * client.textRenderer.fontHeight;
        if (!description.isEmpty()) {
            if (bl2) {
                drawContext.drawGuiTexture(TITLE_BOX_TEXTURE, m, l + 26 - n, this.width, n);
            } else {
                drawContext.drawGuiTexture(TITLE_BOX_TEXTURE, m, l, this.width, n);
            }
        }
        drawContext.drawGuiTexture(status.getBoxTexture(), 200, 26, 0, 0, m, l, j, 26);
        drawContext.drawGuiTexture(status.getBoxTexture(), 200, 26, 200 - k, 0, m + j, l, k, 26);
        drawContext.drawGuiTexture(status.getFrameTexture(PROFESSIONS_FRAME), originX + this.x + 3, originY + this.y, 26, 26);
        final var matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(0.0, 0.0, 200.0);
        if(unlocked)
            drawContext.drawTextWithShadow(getTextRenderer(), String.valueOf(getAmount()), m + 6, originY + this.y + 4, 0xFFFFFFFF);
        matrices.pop();
        if (bl) {
            drawContext.drawTextWithShadow(getTextRenderer(), title, m + 5, originY + this.y + 9, 0xffffffff);
        } else {
            drawContext.drawTextWithShadow(getTextRenderer(), title, originX + this.x + 32, originY + this.y + 9, -1);
        }
        if (bl2) {
            for (int o = 0; o < description.size(); ++o) {
                drawContext.drawTextWithShadow(getTextRenderer(), description.get(o), m + 5, l + 26 - n + 7 + o * this.client.textRenderer.fontHeight, 0xffaaaaaa);
            }
        } else {
            for (int o = 0; o < description.size(); ++o) {
                drawContext.drawTextWithShadow(getTextRenderer(), description.get(o), m + 5, originY + this.y + 9 + 17 + o * this.client.textRenderer.fontHeight, 0xffaaaaaa);
            }
        }
        drawContext.drawItem(this.profession.getIcon(), originX + this.x + 8, originY + this.y + 5);
    }

    private int getAmount() {
        if(profession.getParent() == null) {
            return professionManager.getFreeColonists();
        } else {
            return profession.getAmount();
        }
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    public ProfessionResearchState isUnlocked() {
        return this.professionManager.isRequirementsFulfilled(
                this.profession,
                CountProfessionals.DONT_COUNT
        );
    }

    public void onClick() {
        professionManager.openBuildingHireScreen(profession.getId());
    }

    public ProfessionWidget getParent() {
        return parent;
    }
}
