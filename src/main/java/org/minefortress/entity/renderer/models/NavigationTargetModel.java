package org.minefortress.entity.renderer.models;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.minefortress.entity.fight.NavigationTargetEntity;

import java.util.Collections;

public class NavigationTargetModel extends EntityModel<NavigationTargetEntity> {

    private final ModelPart arrow;

    public NavigationTargetModel() {
        super();
        final var cuboids = ModelPartBuilder
                .create()
                .cuboid(0, 0, 0, 1, 1, 1)
                .build()
                .stream()
                .map(it -> it.createCuboid(32, 32))
                .toList();

        this.arrow = new ModelPart(cuboids, Collections.emptyMap());
    }

    @Override
    public void setAngles(NavigationTargetEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.arrow.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    public Identifier getTexture() {
        return null;
    }
}
