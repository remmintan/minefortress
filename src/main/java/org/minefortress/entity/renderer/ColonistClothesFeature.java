package org.minefortress.entity.renderer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;

public class ColonistClothesFeature extends FeatureRenderer<Colonist, BipedEntityModel<Colonist>> {

    private static final Identifier ARCHER = new Identifier("minefortress", "textures/skins/archer.png");
    private static final Identifier ARMORER = new Identifier("minefortress", "textures/skins/armorer.png");
    private static final Identifier BAKER = new Identifier("minefortress", "textures/skins/baker.png");
    private static final Identifier BLACKSMITH = new Identifier("minefortress", "textures/skins/blacksmith.png");
    private static final Identifier BUTCHER = new Identifier("minefortress", "textures/skins/butcher.png");
    private static final Identifier COLONIST = new Identifier("minefortress", "textures/skins/colonist.png");
    private static final Identifier COOK = new Identifier("minefortress", "textures/skins/cook.png");
    private static final Identifier CRAFTER = new Identifier("minefortress", "textures/skins/crafter.png");
    private static final Identifier CROSSBOWMAN = new Identifier("minefortress", "textures/skins/crossbowman.png");
    private static final Identifier FARMER = new Identifier("minefortress", "textures/skins/farmer.png");
    private static final Identifier FISHERMAN = new Identifier("minefortress", "textures/skins/fisherman.png");
    private static final Identifier FOOTMAN = new Identifier("minefortress", "textures/skins/footman.png");
    private static final Identifier FORESTER = new Identifier("minefortress", "textures/skins/forester.png");
    private static final Identifier HUNTER = new Identifier("minefortress", "textures/skins/hunter.png");
    private static final Identifier KNIGHT = new Identifier("minefortress", "textures/skins/knight.png");
    private static final Identifier LEATHER_WORKER = new Identifier("minefortress", "textures/skins/leather_worker.png");
    private static final Identifier LUMBERJACK = new Identifier("minefortress", "textures/skins/lumberjack.png");
    private static final Identifier LUMBERJACK_T2 = new Identifier("minefortress", "textures/skins/lumberjack_t2.png");
    private static final Identifier LUMBERJACK_T3 = new Identifier("minefortress", "textures/skins/lumberjack_t3.png");
    private static final Identifier MINER = new Identifier("minefortress", "textures/skins/miner.png");
    private static final Identifier MINER_T2 = new Identifier("minefortress", "textures/skins/miner_t2.png");
    private static final Identifier MINER_T3 = new Identifier("minefortress", "textures/skins/miner_t3.png");
    private static final Identifier RIDER = new Identifier("minefortress", "textures/skins/rider.png");
    private static final Identifier SADDLER = new Identifier("minefortress", "textures/skins/saddler.png");
    private static final Identifier SHEPHERD = new Identifier("minefortress", "textures/skins/shepherd.png");
    private static final Identifier STABLEMAN = new Identifier("minefortress", "textures/skins/stableman.png");
    private static final Identifier TAILOR = new Identifier("minefortress", "textures/skins/tailor.png");
    private static final Identifier WARRIOR = new Identifier("minefortress", "textures/skins/warrior.png");
    private static final Identifier WEAVER = new Identifier("minefortress", "textures/skins/weaver.png");



    public ColonistClothesFeature(
            FeatureRendererContext<Colonist, BipedEntityModel<Colonist>> context
    ) {
        super(context);
    }


    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Colonist entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if(entity.isSleeping()) return;
        VillagerClothingFeatureRenderer.renderModel(this.getContextModel(), this.getArmorTexture(), matrices, vertexConsumers, light, entity, 1.0f, 1.0f, 1.0f);
    }


    private Identifier getArmorTexture() {
        return MINER_T2;
    }
}
