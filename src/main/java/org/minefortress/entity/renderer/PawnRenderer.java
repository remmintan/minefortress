package org.minefortress.entity.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.minefortress.MineFortressMod;
import org.minefortress.entity.BasePawnEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IPawn;
import org.minefortress.fortress.ClientFortressManager;
import org.minefortress.interfaces.IFortressMinecraftClient;

import java.util.Optional;

public class PawnRenderer extends BipedEntityRenderer<IPawn, PawnModel> {

    private static final Identifier GUY = new Identifier("minefortress", "textures/skins/guy.png");
    private static final Identifier GUY2 = new Identifier("minefortress", "textures/skins/guy2.png");
    private static final Identifier GUY3 = new Identifier("minefortress", "textures/skins/guy3.png");
    private static final Identifier GUY4 = new Identifier("minefortress", "textures/skins/guy4.png");

    public PawnRenderer(EntityRendererFactory.Context context) {
        super(context, new PawnModel(context), 0.5f);
        this.addFeature(new PawnClothesFeature(this));
    }

    @Override
    public Identifier getTexture(IPawn pawn) {
        final var bodyTextureId = pawn.getBodyTextureId();
        return switch (bodyTextureId) {
            case 0 -> GUY;
            case 1 -> GUY2;
            case 2 -> GUY3;
            default -> GUY4;
        };
    }

    @Override
    protected boolean hasLabel(IPawn colonist) {
        return colonist.hasCustomName();
    }

    @Override
    public void render(BasePawnEntity pawn, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        setClothesVilibility(pawn);
        super.render(pawn, f, g, matrixStack, vertexConsumerProvider, i);

        final MinecraftClient client = getClient();
        final GameMode currentGamemode = Optional
                .ofNullable(client.interactionManager)
                .map(ClientPlayerInteractionManager::getCurrentGameMode)
                .orElse(GameMode.DEFAULT);

        if(currentGamemode == MineFortressMod.FORTRESS) {
            final boolean hovering = client.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == pawn;
            final var fightSelecting = selectedAsTargeted(pawn);
            final boolean selecting = getFortressClientManager().getSelectedPawn() == pawn;
            var color = getHealthFoodLevelColor(pawn);
            if(hovering || selecting || color != null || fightSelecting) {
                final VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
                if(color != null && !(hovering || fightSelecting)) {
                    color.mul(0.7f);
                }

                if(color == null) {
                    color = new Vector3f(selecting ? 0.7f : 0.0f, selecting ? 0.7f : 1.0f, selecting ? 0.7f : 0.0f);
                    if(fightSelecting) {
                        color = new Vector3f(0.0f, 1.0f, 0.0f);
                    }
                }

                PawnRenderer.renderRhombus(matrixStack, buffer, pawn, color);
            }
        }
    }

    private boolean selectedAsTargeted (IPawn pawn) {
        return pawn instanceof ITargetedPawn tp && getFortressClientManager().getFightManager().getSelectionManager().isSelected(tp);
    }

    private float getHealthFoodLevel(IPawn colonist) {
        final var health = colonist.getHealth();
        final var foodLevel = colonist.getCurrentFoodLevel();

        return Math.min(health, foodLevel);
    }

    @Nullable
    private Vector3f getHealthFoodLevelColor(IPawn colonist) {
        final var healthFoodLevel = getHealthFoodLevel(colonist);
        final var maxLevelOfEachColor = (float)0xFF;
        if(healthFoodLevel > 10) return null;
        if(healthFoodLevel <= 10 && healthFoodLevel >= 5) {
            final var red = 0xFF / maxLevelOfEachColor;
            final var green = 0xAA / maxLevelOfEachColor;
            final var blue = 0x00 / maxLevelOfEachColor;
            return new Vector3f(red, green, blue);
        }
        final var red = 0xFF / maxLevelOfEachColor;
        final var green = 0x55 / maxLevelOfEachColor;
        final var blue = 0x55 / maxLevelOfEachColor;
        return new Vector3f(red, green, blue);
    }

    @Nullable
    @Override
    protected RenderLayer getRenderLayer(BasePawnEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        return super.getRenderLayer(entity, showBody, translucent, showOutline);
    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    private IFortressMinecraftClient getFortressClient() {
        return (IFortressMinecraftClient) getClient();
    }

    private ClientFortressManager getFortressClientManager() {
        return getFortressClient().get_FortressClientManager();
    }

    private void setClothesVilibility(MobEntity colonist) {
        final var colonistModel = (PlayerEntityModel<IPawn>)this.getModel();
        colonistModel.hat.visible = true;
        colonistModel.jacket.visible = !colonist.isSleeping();
        colonistModel.leftPants.visible = !colonist.isSleeping();
        colonistModel.rightPants.visible = !colonist.isSleeping();
        colonistModel.leftSleeve.visible = !colonist.isSleeping();
        colonistModel.rightSleeve.visible = !colonist.isSleeping();
    }

    private static void renderRhombus(MatrixStack matrices, VertexConsumer vertices, Entity entity, Vector3f color) {
        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
        if (entity instanceof LivingEntity) {
            matrices.push();
            final double xCenter = (box.minX + box.maxX) / 2;
            final double zCenter = (box.minZ + box.maxZ) / 2;
            matrices.translate(xCenter, box.maxY * 1.5, zCenter);

            float radians = (float) Math.toRadians(45);

            final Quaternionf xRotation = new Quaternionf().set(new AxisAngle4f(radians, 1, 0, 0));
            final Quaternionf yRoation = new Quaternionf().set(new AxisAngle4f(radians, 0, 1, 0));
            matrices.multiply(xRotation);
            matrices.multiply(yRoation);
            matrices.scale(0.3f, 0.3f, 0.3f);

            WorldRenderer.drawBox(matrices, vertices, -0.5f,  -0.5f, -0.5f, 0.5f,  0.5f, 0.5f, color.x(), color.y(), color.z(), 1.0f);
            matrices.pop();
        }
    }

}