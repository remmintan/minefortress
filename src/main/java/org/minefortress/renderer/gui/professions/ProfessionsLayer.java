package org.minefortress.renderer.gui.professions;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.professions.ClientProfessionManager;
import org.minefortress.professions.Profession;

import java.util.LinkedList;
import java.util.List;

public class ProfessionsLayer extends DrawableHelper {

    private static final Identifier LAYER_BACKGROUND = new Identifier("textures/gui/advancements/backgrounds/stone.png");
    private final static int LAYER_WIDTH = 234;
    private final static int LAYER_HEIGHT = 113;

    private final ProfessionWidget root;
    private final List<ProfessionWidget> widgets = new LinkedList<>();

    private int minPanX = Integer.MAX_VALUE;
    private int minPanY = Integer.MAX_VALUE;
    private int maxPanX = Integer.MIN_VALUE;
    private int maxPanY = Integer.MIN_VALUE;

    private double originX;
    private double originY;
    private boolean initialized = false;

    private float alpha;


    public ProfessionsLayer(FortressMinecraftClient client) {
        final ClientProfessionManager professionManager = client.getFortressClientManager().getProfessionManager();
        ProfessionWidget root = createProfessionsTree(professionManager);

        ProfessionsPositioner.arrangeForTree(root);
        for(ProfessionWidget widget:widgets) {
            final int x = widget.getX();
            final int y = widget.getY();
            this.minPanX = Math.min(this.minPanX, x);
            this.maxPanX = Math.max(this.maxPanX, x + (int)ProfessionWidget.PROFESSION_WIDGET_WIDTH);
            this.minPanY = Math.min(this.minPanY, y);
            this.maxPanY = Math.max(this.maxPanY, y + (int)ProfessionWidget.PROFESSION_WIDGET_HEIGHT);
        }

        final int paxExpand = 40;
        this.maxPanX += paxExpand;
//        this.minPanX += paxExpand;
        this.maxPanY += paxExpand;
//        this.minPanY += paxExpand;
        this.root = root;
    }

    public void render(MatrixStack matrices) {
        this.init();
        matrices.push();
        maskBefore(matrices);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, LAYER_BACKGROUND);
        int originX = MathHelper.floor(this.originX);
        int originY = MathHelper.floor(this.originY);
        int startX = originX % 16;
        int startY = originY % 16;
        for (int x = -1; x <= 15; ++x) {
            for (int y = -1; y <= 8; ++y) {
                ProfessionsLayer.drawTexture(matrices, startX + 16 * x, startY + 16 * y, 0.0f, 0.0f, 16, 16, 16, 16);
            }
        }

        this.root.renderLines(matrices, originX, originY, true);
        this.root.renderLines(matrices, originX, originY, false);
        this.root.renderWidgets(matrices, originX, originY);

        maskAfter(matrices);
        matrices.pop();
    }

    public void move(double offsetX, double offsetY) {
        if (this.maxPanX - this.minPanX > LAYER_WIDTH) {
            this.originX = MathHelper.clamp(this.originX + offsetX, (double)(-(this.maxPanX - LAYER_WIDTH)), 40.0);
        }
        if (this.maxPanY - this.minPanY > LAYER_HEIGHT) {
            this.originY = MathHelper.clamp(this.originY + offsetY, (double)(-(this.maxPanY - LAYER_HEIGHT)), 40.0);
        }
    }

    private void maskAfter(MatrixStack matrices) {
        RenderSystem.depthFunc(GL11.GL_GEQUAL);
        matrices.translate(0.0, 0.0, -950.0);
        RenderSystem.colorMask(false, false, false, false);
        AdvancementTab.fill(matrices, 4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    private void maskBefore(MatrixStack matrices) {
        matrices.translate(0.0, 0.0, 950.0);
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(false, false, false, false);
        AdvancementTab.fill(matrices, 4680, 2260, -4680, -2260, 0xff000000);
        RenderSystem.colorMask(true, true, true, true);
        matrices.translate(0.0, 0.0, -950.0);
        RenderSystem.depthFunc(GL11.GL_GEQUAL);
        AdvancementTab.fill(matrices, LAYER_WIDTH, LAYER_HEIGHT, 0, 0, 0xff000000);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    private void init() {
        if (!this.initialized) {
            this.originX = 117 - (double)(this.maxPanX + this.minPanX) / 2;
            this.originY = 56 - (double)(this.maxPanY + this.minPanY) / 2;
            this.initialized = true;
        }
    }

    @NotNull
    private ProfessionWidget createProfessionsTree(ClientProfessionManager professionManager) {
        final Profession rootProfession = professionManager.getRootProfession();
        final ProfessionWidget rootWidget = new ProfessionWidget(rootProfession);

        createTreeNode(rootWidget, rootProfession);

        return rootWidget;
    }

    private void createTreeNode(ProfessionWidget parentWidget, Profession parent) {
        final List<Profession> children = parent.getChildren();
        this.widgets.add(parentWidget);
        for(Profession child:children) {
            final ProfessionWidget childWidget = new ProfessionWidget(child);

            childWidget.setParent(parentWidget);
            parentWidget.addChild(childWidget);

            createTreeNode(childWidget, child);
        }
    }

    public void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int screenWidth) {
        matrices.push();
        matrices.translate(0.0, 0.0, -200.0);
        AdvancementTab.fill(matrices, 0, 0, LAYER_WIDTH, LAYER_HEIGHT, MathHelper.floor(this.alpha * 255.0f) << 24);

        int oX = MathHelper.floor(this.originX);
        int oY = MathHelper.floor(this.originY);
        boolean bl = false;
        mouseY = mouseY + 35;
        if (mouseX > 0 && mouseX < LAYER_WIDTH && mouseY > 0 && mouseY < LAYER_HEIGHT) {
            for (ProfessionWidget advancementWidget : this.widgets) {
                if (!advancementWidget.shouldRender(oX, oY, mouseX, mouseY)) continue;
                bl = true;
                advancementWidget.drawTooltip(matrices, oX, oY, this.alpha, x, y, screenWidth);
                break;
            }
        }
        matrices.pop();

        this.alpha = bl ? MathHelper.clamp(this.alpha + 0.02f, 0.0f, 0.3f) : MathHelper.clamp(this.alpha - 0.04f, 0.0f, 1.0f);
    }

}
