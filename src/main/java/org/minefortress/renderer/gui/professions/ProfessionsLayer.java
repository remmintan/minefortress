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

    /**
     * colonist -> miner1, lumberjack1, forester, crafter
     * miner1 -> miner2
     * lumberjack1 -> lumberjack2
     * forester -> hunter, fisherman, farmer
     * crafter -> leather_worker1
     * miner2 -> miner3
     * lumberjack2 -> lumberjack3
     * hunter -> warrior1, archer1, knight1
     * farmer -> baker, shepherd
     * leather_worker1 -> leather_worker2, blacksmith, weaver
     * warrior1 -> warrior2
     * archer1 -> archer2
     * knight1 -> knight2
     * shepherd -> stableman, butcher
     * blacksmith -> armorer
     * weaver -> tailor
     * butcher -> cook
     */
    @NotNull
    private ProfessionWidget createProfessionsTree(ClientProfessionManager professionManager) {
        ProfessionWidget root = new ProfessionWidget(professionManager.getProfession("colonist"));

        // colonist -> miner1, lumberjack1, forester, crafter
        final ProfessionWidget miner1 = new ProfessionWidget(professionManager.getProfession("miner1"));
        final ProfessionWidget lumberjack1 = new ProfessionWidget(professionManager.getProfession("lumberjack1"));
        final ProfessionWidget forester = new ProfessionWidget(professionManager.getProfession("forester"));
        final ProfessionWidget crafter = new ProfessionWidget(professionManager.getProfession("crafter"));
        addChildren(root, miner1, lumberjack1, forester, crafter);

        // miner1 -> miner2
        final ProfessionWidget miner2 = new ProfessionWidget(professionManager.getProfession("miner2"));
        addChildren(miner1, miner2);

        // lumberjack1 -> lumberjack2
        final ProfessionWidget lumberjack2 = new ProfessionWidget(professionManager.getProfession("lumberjack2"));
        addChildren(lumberjack1, lumberjack2);

        // forester -> hunter, fisherman, farmer
        final ProfessionWidget hunter = new ProfessionWidget(professionManager.getProfession("hunter"));
        final ProfessionWidget fisherman = new ProfessionWidget(professionManager.getProfession("fisherman"));
        final ProfessionWidget farmer = new ProfessionWidget(professionManager.getProfession("farmer"));
        addChildren(forester, hunter, fisherman, farmer);

        // crafter -> leather_worker1
        final ProfessionWidget leather_worker1 = new ProfessionWidget(professionManager.getProfession("leather_worker1"));
        addChildren(crafter, leather_worker1);

        // miner2 -> miner3
        final ProfessionWidget miner3 = new ProfessionWidget(professionManager.getProfession("miner3"));
        addChildren(miner2, miner3);

        // lumberjack2 -> lumberjack3
        final ProfessionWidget lumberjack3 = new ProfessionWidget(professionManager.getProfession("lumberjack3"));
        addChildren(lumberjack2, lumberjack3);

        // hunter -> warrior1, archer1, knight1
        final ProfessionWidget warrior1 = new ProfessionWidget(professionManager.getProfession("warrior1"));
        final ProfessionWidget archer1 = new ProfessionWidget(professionManager.getProfession("archer1"));
        final ProfessionWidget knight1 = new ProfessionWidget(professionManager.getProfession("knight1"));
        addChildren(hunter, warrior1, archer1, knight1);

        // farmer -> baker, shepherd
        final ProfessionWidget baker = new ProfessionWidget(professionManager.getProfession("baker"));
        final ProfessionWidget shepherd = new ProfessionWidget(professionManager.getProfession("shepherd"));
        addChildren(farmer, baker, shepherd);

        // leather_worker1 -> leather_worker2, blacksmith, weaver
        final ProfessionWidget leather_worker2 = new ProfessionWidget(professionManager.getProfession("leather_worker2"));
        final ProfessionWidget blacksmith = new ProfessionWidget(professionManager.getProfession("blacksmith"));
        final ProfessionWidget weaver = new ProfessionWidget(professionManager.getProfession("weaver"));
        addChildren(leather_worker1, leather_worker2, blacksmith, weaver);

        // warrior1 -> warrior2
        final ProfessionWidget warrior2 = new ProfessionWidget(professionManager.getProfession("warrior2"));
        addChildren(warrior1, warrior2);

        // archer1 -> archer2
        final ProfessionWidget archer2 = new ProfessionWidget(professionManager.getProfession("archer2"));
        addChildren(archer1, archer2);

        // knight1 -> knight2
        final ProfessionWidget knight2 = new ProfessionWidget(professionManager.getProfession("knight2"));
        addChildren(knight1, knight2);

        // shepherd -> stableman, butcher
        final ProfessionWidget stableman = new ProfessionWidget(professionManager.getProfession("stableman"));
        final ProfessionWidget butcher = new ProfessionWidget(professionManager.getProfession("butcher"));
        addChildren(shepherd, stableman, butcher);

        // blacksmith -> armorer
        final ProfessionWidget armorer = new ProfessionWidget(professionManager.getProfession("armorer"));
        addChildren(blacksmith, armorer);

        // weaver -> tailor
        final ProfessionWidget tailor = new ProfessionWidget(professionManager.getProfession("tailor"));
        addChildren(weaver, tailor);

        // butcher -> cook
        final ProfessionWidget cook = new ProfessionWidget(professionManager.getProfession("cook"));
        addChildren(butcher, cook);
        return root;
    }

    private void addChildren(ProfessionWidget root, ProfessionWidget... children) {
        for(ProfessionWidget child : children) {
            child.setParent(root);
            root.addChild(child);
        }
    }

}
