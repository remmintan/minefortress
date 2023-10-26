package org.minefortress.renderer.gui.professions;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.remmintan.mods.minefortress.core.interfaces.professions.CountProfessionals;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfession;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfessionsManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.ProfessionResearchState;
import org.minefortress.utils.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class ProfessionWidget {

    public static final float PROFESSION_WIDGET_WIDTH = 64f;
    public static final float PROFESSION_WIDGET_HEIGHT = 35f;
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");

    private ProfessionWidget parent;
    private final List<ProfessionWidget> children = new ArrayList<>();
    private final IProfession profession;
    private final IProfessionsManager professionManager;

    private int x = 0;
    private int y = 0;

    private final int width;
    private final MinecraftClient client;

    public ProfessionWidget(IProfession profession, IProfessionsManager professionManager) {
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
        final boolean unlockedWithCount = isUnlocked(true) == ProfessionResearchState.UNLOCKED;
        final boolean unlocked = isUnlocked(false) == ProfessionResearchState.UNLOCKED;

        AdvancementObtainedStatus status = unlockedWithCount ? AdvancementObtainedStatus.OBTAINED : AdvancementObtainedStatus.UNOBTAINED;
        final int v = 128 + status.getSpriteIndex() * 26;
        final int u = profession.getType().getTextureV();
        drawContext.drawTexture(WIDGETS_TEXTURE, x + this.x + 3, y + this.y, u, v, 26, 26);

        drawContext.drawItem(profession.getIcon(), x + this.x + 8, y + this.y + 5);
        final var matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(0.0, 0.0, 200.0);
        if(unlocked)
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
//        final Profession parent = profession.getParent();
//        if(parent != null && !this.professionManager.isRequirementsFulfilled(parent, ProfessionManager.CountProfessionals.DONT_COUNT, false)){
//            return true;
//        }

        int i = originX + this.x;
        int j = i + 26;
        int k = originY + this.y;
        int l = k + 26;
        return mouseX < i || mouseX > j || mouseY < k || mouseY > l;
    }

    public void drawTooltip(DrawContext drawContext, int originX, int originY, int x, int screenWidth) {
        final var unlocked = isUnlocked(false) == ProfessionResearchState.UNLOCKED;
        final var unlockedWithCount = isUnlocked(true) == ProfessionResearchState.UNLOCKED;

        final boolean parentUnlocked = isUnlocked(false) != ProfessionResearchState.LOCKED_PARENT;

        AdvancementObtainedStatus status = unlockedWithCount?AdvancementObtainedStatus.OBTAINED:AdvancementObtainedStatus.UNOBTAINED;
        int j = MathHelper.floor((float)this.width);
        int k = this.width - j;
        int l = originY + this.y;
        boolean bl = x + originX + this.x + this.width + 26 >= screenWidth;
        int m = bl ? originX + this.x - this.width + 26 + 6 : originX + this.x;

        final String title = this.profession.getTitle();
        List<Text> description;
        if(unlockedWithCount) {
            description = this.profession.getDescription();
        } else if(unlocked) {
            description = this.profession.getUnlockMoreMessage();
        } else if (!parentUnlocked) {
            description = GuiUtils.splitTextInWordsForLength("You need to unlock the parent profession first!");
        } else {
            description = this.profession.getUnlockMessage();
        }

        int n = 32 + description.size() * this.client.textRenderer.fontHeight;
        boolean bl2 = 113 - originY - this.y - 26 <= 6 + description.size() * client.textRenderer.fontHeight;
        if (!description.isEmpty()) {
            if (bl2) {
                this.method_2324(drawContext, WIDGETS_TEXTURE, m, l + 26 - n, this.width, n);
            } else {
                this.method_2324(drawContext, WIDGETS_TEXTURE, m, l, this.width, n);
            }
        }
        drawContext.drawTexture(WIDGETS_TEXTURE, m, l, 0, status.getSpriteIndex() * 26, j, 26);
        drawContext.drawTexture(WIDGETS_TEXTURE, m + j, l, 200 - k, status.getSpriteIndex() * 26, k, 26);
        drawContext.drawTexture(WIDGETS_TEXTURE, originX + this.x + 3, originY + this.y, this.profession.getType().getTextureV(), 128 + status.getSpriteIndex() * 26, 26, 26);
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

    protected void method_2324(DrawContext drawContext, Identifier identifier, int x, int y, int i, int j) {
        drawContext.drawTexture(identifier, x, y, 0, 52, 10, 10);
        this.method_2321(drawContext, identifier, x + 10, y, i - 10 - 10, 10, 10, 52, 200 - 10 - 10, 26);
        drawContext.drawTexture(identifier, x + i - 10, y, 200 - 10, 52, 10, 10);
        drawContext.drawTexture(identifier, x, y + j - 10, 0, 52 + 26 - 10, 10, 10);
        this.method_2321(drawContext, identifier, x + 10, y + j - 10, i - 10 - 10, 10, 10, 52 + 26 - 10, 200 - 10 - 10, 26);
        drawContext.drawTexture(identifier, x + i - 10, y + j - 10, 200 - 10, 52 + 26 - 10, 10, 10);
        this.method_2321(drawContext, identifier, x, y + 10, 10, j - 10 - 10, 0, 52 + 10, 200, 26 - 10 - 10);
        this.method_2321(drawContext, identifier, x + 10, y + 10, i - 10 - 10, j - 10 - 10, 10, 52 + 10, 200 - 10 - 10, 26 - 10 - 10);
        this.method_2321(drawContext, identifier, x + i - 10, y + 10, 10, j - 10 - 10, 200 - 10, 52 + 10, 200, 26 - 10 - 10);
    }

    protected void method_2321(DrawContext drawContext, Identifier identifier, int x, int y, int i, int j, int k, int l, int m, int n) {
        for (int o = 0; o < i; o += m) {
            int p = x + o;
            int q = Math.min(m, i - o);
            for (int r = 0; r < j; r += n) {
                int s = y + r;
                int t = Math.min(n, j - r);
                drawContext.drawTexture(identifier, p, s, k, l, q, t);
            }
        }
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    public ProfessionResearchState isUnlocked(boolean countProfessionals) {
        var shouldCountProfs = countProfessionals ? CountProfessionals.INCREASE : CountProfessionals.DONT_COUNT;
        if(profession.isHireMenu()) {
            shouldCountProfs = CountProfessionals.DONT_COUNT;
        }
        return this.professionManager.isRequirementsFulfilled(
                this.profession,
                shouldCountProfs,
                true
        );
    }

    public void onClick(int button) {
        if(button == 0) {
            professionManager.findIdFromProfession(this.profession)
                            .ifPresent(it -> professionManager.increaseAmount(it, false));
        } else if(button == 1) {
            professionManager.findIdFromProfession(this.profession)
                            .ifPresent(professionManager::decreaseAmount);
        }
    }

    public ProfessionWidget getParent() {
        return parent;
    }
}
