package org.minefortress.entity.renderer;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.minefortress.entity.fight.NavigationTargetEntity;
import org.minefortress.entity.renderer.models.NavigationTargetModel;

public class NavigationTargetRenderer extends EntityRenderer<NavigationTargetEntity> {

    private static final Identifier TEXTURE = new Identifier("minefortress", "textures/combat/navigation_target.png");

    private final NavigationTargetModel model = new NavigationTargetModel();
    public NavigationTargetRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(NavigationTargetEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        final var layer = model.getLayer(TEXTURE);
        final var buffer = vertexConsumers.getBuffer(layer);
        final var overlay = OverlayTexture.packUv(OverlayTexture.getU(0), OverlayTexture.getV(false));
        model.setAngles(entity, 0, 0, tickDelta, yaw, 180-90);
        model.render(matrices, buffer, light, overlay, 1, 1, 1, 1);
    }

    @Override
    public boolean shouldRender(NavigationTargetEntity entity, Frustum frustum, double x, double y, double z) {
        if (super.shouldRender(entity, frustum, x, y, z)) {
            return true;
        } else {
            return entity != null && frustum.isVisible(entity.getVisibilityBoundingBox());
        }
    }

    @Override
    public Identifier getTexture(NavigationTargetEntity entity) {
        return TEXTURE;
    }
}
