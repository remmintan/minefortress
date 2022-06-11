package org.minefortress.entity.renderer;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.minefortress.MineFortressMod;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;

import java.awt.*;
import java.util.Optional;

public class ColonistRenderer extends BipedEntityRenderer<Colonist, BipedEntityModel<Colonist>> {

    private static final Identifier GUY = new Identifier("minefortress", "textures/skins/guy.png");
    private static final Identifier GUY2 = new Identifier("minefortress", "textures/skins/guy2.png");
    private static final Identifier GUY3 = new Identifier("minefortress", "textures/skins/guy3.png");
    private static final Identifier GUY4 = new Identifier("minefortress", "textures/skins/guy4.png");

    public ColonistRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.addFeature(new ColonistClothesFeature(this));
    }

    @Override
    public Identifier getTexture(Colonist colonist) {
        final var guyType = colonist.getGuyType();
        return switch (guyType) {
            case 0 -> GUY;
            case 1 -> GUY2;
            case 2 -> GUY3;
            default -> GUY4;
        };
    }

    @Override
    protected boolean hasLabel(Colonist colonist) {
        return colonist.hasCustomName();
    }

    @Override
    public void render(Colonist colonist, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        setClothesVilibility(colonist);
        super.render(colonist, f, g, matrixStack, vertexConsumerProvider, i);

        final MinecraftClient client = getClient();
        final GameMode currentGamemode = Optional
                .ofNullable(client.interactionManager)
                .map(ClientPlayerInteractionManager::getCurrentGameMode)
                .orElse(GameMode.DEFAULT);

        if(currentGamemode == MineFortressMod.FORTRESS) {
            final boolean hovering = client.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == colonist;
            final var fightSelecting = getFortressClientManager().getFightManager().getSelectionManager().isSelected(colonist);
            final boolean selecting = getFortressClientManager().getSelectedColonist() == colonist;
            var color = getHealthFoodLevelColor(colonist);
            if(hovering || selecting || color != null || fightSelecting) {
                final VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
                if(color != null && (hovering || fightSelecting)) {
                    color.scale(0.7f);
                }

                if(color == null) {
                    color = new Vec3f(selecting ? 0.7f : 0.0f, selecting ? 0.7f : 1.0f, selecting ? 0.7f : 0.0f);
                    if(fightSelecting) {
                        color = new Vec3f(0.0f, 1.0f, 0.0f);
                    }
                }

                ColonistRenderer.renderRhombus(matrixStack, buffer, colonist, color);
            }
        }
    }

    private float getHealthFoodLevel(Colonist colonist) {
        final var health = colonist.getHealth();
        final var foodLevel = colonist.getCurrentFoodLevel();

        return Math.min(health, foodLevel);
    }

    @Nullable
    private Vec3f getHealthFoodLevelColor(Colonist colonist) {
        final var healthFoodLevel = getHealthFoodLevel(colonist);
        final var maxLevelOfEachColor = (float)0xFF;
        if(healthFoodLevel > 10) return null;
        if(healthFoodLevel <= 10 && healthFoodLevel >= 5) {
            final var red = 0xFF / maxLevelOfEachColor;
            final var green = 0xAA / maxLevelOfEachColor;
            final var blue = 0x00 / maxLevelOfEachColor;
            return new Vec3f(red, green, blue);
        }
        final var red = 0xFF / maxLevelOfEachColor;
        final var green = 0x55 / maxLevelOfEachColor;
        final var blue = 0x55 / maxLevelOfEachColor;
        return new Vec3f(red, green, blue);
    }

    @Nullable
    @Override
    protected RenderLayer getRenderLayer(Colonist entity, boolean showBody, boolean translucent, boolean showOutline) {
        return super.getRenderLayer(entity, showBody, translucent, showOutline);
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

    private void setClothesVilibility(MobEntity colonist) {
        final PlayerEntityModel colonistModel = (PlayerEntityModel)this.getModel();
        colonistModel.hat.visible = true;
        colonistModel.jacket.visible = !colonist.isSleeping();
        colonistModel.leftPants.visible = !colonist.isSleeping();
        colonistModel.rightPants.visible = !colonist.isSleeping();
        colonistModel.leftSleeve.visible = !colonist.isSleeping();
        colonistModel.rightSleeve.visible = !colonist.isSleeping();
    }

    private static void renderRhombus(MatrixStack matrices, VertexConsumer vertices, Entity entity, Vec3f color) {
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

            WorldRenderer.drawBox(matrices, vertices, -0.5f,  -0.5f, -0.5f, 0.5f,  0.5f, 0.5f, color.getX(), color.getY(), color.getZ(), 1.0f);
            matrices.pop();
        }
    }

}