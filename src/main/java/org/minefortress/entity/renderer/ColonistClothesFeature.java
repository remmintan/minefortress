package org.minefortress.entity.renderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
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
        ColonistClothesFeature.renderModel(this.getContextModel(), this.getArmorTexture(entity.getProfessionId()), matrices, vertexConsumers, light, entity, 1.0f, 1.0f, 1.0f);
    }

    protected static <T extends LivingEntity> void renderModel(EntityModel<T> model, Identifier texture, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float red, float green, float blue) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
        model.render(matrices, vertexConsumer, light, LivingEntityRenderer.getOverlay(entity, 0.0f), red, green, blue, 1f);
    }


    private Identifier getArmorTexture(String professionId) {
        return switch (professionId) {
            case "miner1" -> MINER;
            case "miner2" -> MINER_T2;
            case "miner3" -> MINER_T3;
            case "lumberjack1" -> LUMBERJACK;
            case "lumberjack2" -> LUMBERJACK_T2;
            case "lumberjack3" -> LUMBERJACK_T3;
            case "forester" -> FORESTER;
            case "hunter" -> HUNTER;
            case "fisherman" -> FISHERMAN;
            case "farmer" -> FARMER;
            case "baker" -> BAKER;
            case "shepherd" -> SHEPHERD;
            case "stableman" -> STABLEMAN;
            case "butcher" -> BUTCHER;
            case "cook" -> COOK;
            case "crafter" -> CRAFTER;
            case "leather_worker1", "leather_worker2" -> LEATHER_WORKER;
            case "blacksmith" -> BLACKSMITH;
            case "weaver" -> WEAVER;
            default -> COLONIST;
        };
    }
}
