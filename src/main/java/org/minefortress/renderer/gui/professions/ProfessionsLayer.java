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

import java.util.List;

public class ProfessionsLayer {

    private static final Identifier LAYER_BACKGROUND = new Identifier("textures/gui/advancements/backgrounds/stone.png");

    private int minPanX = Integer.MAX_VALUE;
    private int minPanY = Integer.MAX_VALUE;
    private int maxPanX = Integer.MIN_VALUE;
    private int maxPanY = Integer.MIN_VALUE;

    private double originX;
    private double originY;

    private boolean initialized = false;

    private final ProfessionWidget root;


    public ProfessionsLayer(FortressMinecraftClient client) {
        final ClientProfessionManager professionManager = client.getFortressClientManager().getProfessionManager();
        ProfessionWidget root = createProfessionsTree(professionManager);

        ProfessionsPositioner.arrangeForTree(root);
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
                DrawableHelper.drawTexture(matrices, startX + 16 * x, startY + 16 * y, 0.0f, 0.0f, 16, 16, 16, 16);
            }
        }

        this.root.renderLines(matrices, originX, originY, true);
        this.root.renderLines(matrices, originX, originY, false);
        this.root.renderWidgets(matrices, originX, originY);

        maskAfter(matrices);
        matrices.pop();
    }

    public void move(double offsetX, double offsetY) {
        if (this.maxPanX - this.minPanX > 234) {
            this.originX = MathHelper.clamp(this.originX + offsetX, (double)(-(this.maxPanX - 234)), 0.0);
        }
        if (this.maxPanY - this.minPanY > 113) {
            this.originY = MathHelper.clamp(this.originY + offsetY, (double)(-(this.maxPanY - 113)), 0.0);
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
        AdvancementTab.fill(matrices, 234, 113, 0, 0, 0xff000000);
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

        for(Profession child:children) {
            final ProfessionWidget childWidget = new ProfessionWidget(child);

            childWidget.setParent(parentWidget);
            parentWidget.addChild(childWidget);

            createTreeNode(childWidget, child);
        }
    }

}
