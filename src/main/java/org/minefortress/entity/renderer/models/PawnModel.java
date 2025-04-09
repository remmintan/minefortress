package org.minefortress.entity.renderer.models;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.item.Items;
import org.minefortress.entity.BasePawnEntity;

public class PawnModel extends PlayerEntityModel<BasePawnEntity> {

    public PawnModel(EntityRendererFactory.Context context) {
        super(context.getPart(EntityModelLayers.PLAYER), false);
    }

    @Override
    public void animateModel(BasePawnEntity pawn, float f, float g, float h) {
        this.rightArmPose = ArmPose.EMPTY;

        if(pawn.isAttacking() && pawn.isItemInHand(Items.BOW)) {
            this.rightArmPose = ArmPose.BOW_AND_ARROW;
        }

        super.animateModel(pawn, f, g, h);
    }
}
