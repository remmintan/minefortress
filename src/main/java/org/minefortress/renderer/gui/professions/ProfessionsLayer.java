package org.minefortress.renderer.gui.professions;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.professions.ProfessionManager;
import org.minefortress.professions.Profession;

import java.util.LinkedList;
import java.util.List;

public class ProfessionsLayer extends DrawableHelper {

    private static final Identifier LAYER_BACKGROUND = new Identifier("textures/gui/advancements/backgrounds/stone.png");
    private int layerWidth = 234;
    private int layerHeight = 113;

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
    private final int panExpand = 100;


    public ProfessionsLayer(FortressMinecraftClient client) {
        final ProfessionManager professionManager = client.getFortressClientManager().getProfessionManager();
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


        this.maxPanX += panExpand;
        this.maxPanY += panExpand;
        this.root = root;
    }

    public void setLayerSizes(int width, int height) {
        this.layerWidth = width;
        this.layerHeight = height;
    }

    public void render(MatrixStack matrices) {
        this.init();
        matrices.push();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, LAYER_BACKGROUND);
        int originX = MathHelper.floor(this.originX);
        int originY = MathHelper.floor(this.originY);
        int startX = originX % 16;
        int startY = originY % 16;
        for (int x = -1; x <= (this.layerWidth/16)+1; ++x) {
            for (int y = -1; y <= (this.layerHeight/16)+1; ++y) {
                ProfessionsLayer.drawTexture(matrices, startX + 16 * x, startY + 16 * y, 0.0f, 0.0f, 16, 16, 16, 16);
            }
        }

        this.root.renderLines(matrices, originX, originY, true);
        this.root.renderLines(matrices, originX, originY, false);
        this.root.renderWidgets(matrices, originX, originY);

        matrices.pop();
    }

    public void move(double offsetX, double offsetY) {
        if ((this.maxPanX - this.minPanX + panExpand) > layerWidth) {
            this.originX = MathHelper.clamp(this.originX + offsetX, -(this.maxPanX - layerWidth), panExpand);
        }
        if ((this.maxPanY - this.minPanY + panExpand) > layerHeight) {
            this.originY = MathHelper.clamp(this.originY + offsetY, -(this.maxPanY - layerHeight), panExpand);
        }
    }


    private void init() {
        if (!this.initialized) {
            this.originX = this.layerWidth/2f - (double)(this.maxPanX + this.minPanX - panExpand) / 2;
            this.originY = this.layerHeight/2f - (double)(this.maxPanY + this.minPanY - panExpand) / 2;
            this.initialized = true;
        }
    }

    @NotNull
    private ProfessionWidget createProfessionsTree(ProfessionManager professionManager) {
        final Profession rootProfession = professionManager.getRootProfession();
        final ProfessionWidget rootWidget = new ProfessionWidget(rootProfession, professionManager);

        createTreeNode(rootWidget, rootProfession, professionManager);

        return rootWidget;
    }

    private void createTreeNode(ProfessionWidget parentWidget, Profession parent, ProfessionManager professionManager) {
        final List<Profession> children = parent.getChildren();
        this.widgets.add(parentWidget);
        for(Profession child:children) {
            final ProfessionWidget childWidget = new ProfessionWidget(child, professionManager);

            childWidget.setParent(parentWidget);
            parentWidget.addChild(childWidget);

            createTreeNode(childWidget, child, professionManager);
        }
    }

    public void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int x, int screenWidth) {
        matrices.push();
        matrices.translate(0.0, 0.0, -200.0);
        AdvancementTab.fill(matrices, 0, 0, layerWidth, layerHeight, MathHelper.floor(this.alpha * 255.0f) << 24);

        int oX = MathHelper.floor(this.originX);
        int oY = MathHelper.floor(this.originY);
        boolean bl = false;
        if (mouseX > 0 && mouseX < layerWidth && mouseY > 0 && mouseY < layerHeight) {
            for (ProfessionWidget advancementWidget : this.widgets) {
                if (advancementWidget.shouldNotRender(oX, oY, mouseX, mouseY)) continue;
                bl = true;
                advancementWidget.drawTooltip(matrices, oX, oY,  x, screenWidth);
                break;
            }
        }
        matrices.pop();

        this.alpha = bl ? MathHelper.clamp(this.alpha + 0.02f, 0.0f, 0.3f) : MathHelper.clamp(this.alpha - 0.04f, 0.0f, 1.0f);
    }

    public void onClick(double mouseX, double mouseY, int button) {
        int oX = MathHelper.floor(this.originX);
        int oY = MathHelper.floor(this.originY);
        if (mouseX > 0 && mouseX < layerWidth && mouseY > 0 && mouseY < layerHeight) {
            for (ProfessionWidget professionWidget : this.widgets) {
                if (professionWidget.shouldNotRender(oX, oY, (int) mouseX, (int) mouseY)) continue;
                professionWidget.onClick(button);
                break;
            }
        }
    }

}
