package org.minefortress.entity.renderer;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.minefortress.entity.fight.NavigationTargetEntity;
import org.minefortress.entity.renderer.models.NavigationTargetModel;

public class NavigationTargetRenderer extends EntityRenderer<NavigationTargetEntity> {
    private final NavigationTargetModel model = new NavigationTargetModel();
    public NavigationTargetRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(NavigationTargetEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        final var layer = model.getLayer(model.getTexture());
        final var buffer = vertexConsumers.getBuffer(layer);
        model.render(matrices, buffer, light, 0, 1, 1, 1, 1);
    }

    @Override
    public Identifier getTexture(NavigationTargetEntity entity) {
        return model.getTexture();
    }
}
