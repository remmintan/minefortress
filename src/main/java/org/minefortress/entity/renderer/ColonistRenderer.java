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
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;

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
        return GUY;
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

        final GameMode fortress = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");
        if(currentGamemode == fortress) {
            final boolean hovering = client.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == colonist;
            final boolean selecting = getFortressClientManager().getSelectedColonist() == colonist;
            if(hovering || selecting) {
                final VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
                ColonistRenderer.renderRhombus(matrixStack, buffer, colonist, selecting);
            }
        }
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

}