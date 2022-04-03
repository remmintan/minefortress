package org.minefortress.entity.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;

public class ColonistRenderer extends BipedEntityRenderer<Colonist, BipedEntityModel<Colonist>> {

    private static final Identifier STEVE = new Identifier("textures/entity/steve.png");
    /*
        baker.png
        blacksmith.png
        butcher.png
        crafter.png
        farmer.png
        fisherman.png
        hunter.png
        lumberjack.png
        medieval_underwear.png
        miner.png
     */
    private static final Identifier BAKER = new Identifier("minefortress", "textures/skins/baker.png");
    private static final Identifier BLACKSMITH = new Identifier("minefortress", "textures/skins/blacksmith.png");
    private static final Identifier BUTCHER = new Identifier("minefortress", "textures/skins/butcher.png");
    private static final Identifier CRAFTER = new Identifier("minefortress", "textures/skins/crafter.png");
    private static final Identifier FARMER = new Identifier("minefortress", "textures/skins/farmer.png");
    private static final Identifier FISHERMAN = new Identifier("minefortress", "textures/skins/fisherman.png");
    private static final Identifier HUNTER = new Identifier("minefortress", "textures/skins/hunter.png");
    private static final Identifier LUMBERJACK = new Identifier("minefortress", "textures/skins/lumberjack.png");
    private static final Identifier MEDIEVAL_UNDERWEAR = new Identifier("minefortress", "textures/skins/medieval_underwear.png");
    private static final Identifier MINER = new Identifier("minefortress", "textures/skins/miner.png");


    public ColonistRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
        BipedEntityModel<Colonist> innerArmor = new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR));
        BipedEntityModel<Colonist> outerArmor = new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR));

        this.addFeature(new ArmorFeatureRenderer<>(this, innerArmor, outerArmor));
    }

    @Override
    public Identifier getTexture(Colonist colonist) {
        return convertProfessionToSkin(colonist.getProfessionId());
    }

    @Override
    protected boolean hasLabel(Colonist p_115333_) {
        return p_115333_.hasCustomName();
    }

    @Override
    public void render(Colonist mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
        final MinecraftClient client = getClient();
        final boolean hovering = client.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == mobEntity;
        final boolean selecting = getFortressClientManager().getSelectedColonist() == mobEntity;
        if(hovering || selecting) {
            final VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
            ColonistRenderer.renderRhombus(matrixStack, buffer, mobEntity, selecting);
        }

    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    private FortressMinecraftClient getFortressClient() {
        return (FortressMinecraftClient) getClient();
    }

    private FortressClientManager getFortressClientManager() {
        return getFortressClient().getFortressClientManager();
    }

    private static void renderRhombus(MatrixStack matrices, VertexConsumer vertices, Entity entity, boolean selecting) {
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        if (entity instanceof LivingEntity) {
            matrices.push();
            final double xCenter = (box.minX + box.maxX) / 2;
            final double zCenter = (box.minZ + box.maxZ) / 2;
            matrices.translate(xCenter, box.maxY * 1.5, zCenter);
            final Quaternion xRotation = Vec3f.POSITIVE_X.getDegreesQuaternion(45.0f);
            final Quaternion yRoation = Vec3f.POSITIVE_Y.getDegreesQuaternion(45.0f);
            matrices.multiply(xRotation);
            matrices.multiply(yRoation);
            matrices.scale(0.3f, 0.3f, 0.3f);

            WorldRenderer.drawBox(matrices, vertices, -0.5f,  -0.5f, -0.5f, 0.5f,  0.5f, 0.5f, selecting?0.0f:0.7f, selecting?1.0f:0.7f, selecting?0.0f:0.7f, 1.0f);
            matrices.pop();
        }
    }

    private Identifier convertProfessionToSkin(String professionId) {
        switch(professionId) {
            case "baker":
                return BAKER;
            case "blacksmith":
                return BLACKSMITH;
            case "butcher":
                return BUTCHER;
            case "crafter":
                return CRAFTER;
            case "farmer":
                return FARMER;
            case "fisherman":
                return FISHERMAN;
            case "hunter":
                return HUNTER;
            case "lumberjack1":
            case "lumberjack2":
            case "lumberjack3":
                return LUMBERJACK;
            case "miner1":
            case "miner2":
            case "miner3":
                return MINER;
            case "colonist":
            default:
                return STEVE;
        }
    }
}