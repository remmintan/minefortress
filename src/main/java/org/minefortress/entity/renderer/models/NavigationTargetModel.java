package org.minefortress.entity.renderer.models;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import org.minefortress.entity.fight.NavigationTargetEntity;

public class NavigationTargetModel extends SinglePartEntityModel<NavigationTargetEntity> {

    private static TexturedModelData INSTANCE;

    private final ModelPart root;

    public NavigationTargetModel() {
        this.root = getTexturedModelData().createModel();
    }

    public static TexturedModelData getTexturedModelData() {
        if(INSTANCE == null) {
            ModelData modelData = new ModelData();
            ModelPartData modelPartData = modelData.getRoot();
            final var cuboid = ModelPartBuilder
                    .create()
                    .uv(0, 0)
                    .cuboid(-5, -5, -5, 10, 10, 10);
            modelPartData.addChild(EntityModelPartNames.BODY, cuboid, ModelTransform.NONE);

            INSTANCE = TexturedModelData.of(modelData, 32, 32);
        }

        return INSTANCE;
    }


    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(NavigationTargetEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.root.pitch = headPitch * 0.017453292F;
        this.root.yaw = headYaw * 0.017453292F;
        this.root.pitch += -0.05F - 0.05F * MathHelper.cos(animationProgress * 0.3F);
    }
}
