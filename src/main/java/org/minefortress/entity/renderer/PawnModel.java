package org.minefortress.entity.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.item.Items;
import org.minefortress.entity.BasePawnEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IPawn;

public class PawnModel extends PlayerEntityModel<IPawn> {

    public PawnModel(EntityRendererFactory.Context context) {
        super(context.getPart(EntityModelLayers.PLAYER), false);
    }

    @Override
    public void animateModel(BasePawnEntity pawn, float f, float g, float h) {
//        this.rightArmPose = ArmPose.EMPTY;
//        this.leftArmPose = ArmPose.EMPTY;
//        ItemStack itemStack = mobEntity.getStackInHand(Hand.MAIN_HAND);
//        if (itemStack.isOf(Items.BOW) && mobEntity.isAttacking()) {
//            if (mobEntity.getMainArm() == Arm.RIGHT) {
//                this.rightArmPose = ArmPose.BOW_AND_ARROW;
//            } else {
//                this.leftArmPose = ArmPose.BOW_AND_ARROW;
//            }
//        }

        this.rightArmPose = ArmPose.EMPTY;

        if(pawn.isAttacking() && pawn.isItemInHand(Items.BOW)) {
            this.rightArmPose = ArmPose.BOW_AND_ARROW;
        }

        super.animateModel(pawn, f, g, h);
    }
}
