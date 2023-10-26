package net.remmintan.mods.minefortress.core.interfaces.entities.pawns;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IBaritoneMoveControl;

public interface ITargetedPawn extends IFortressAwareEntity {

    int getId();
    Vec3d getPos();
    LivingEntity getTarget();

    void setAttackTarget(LivingEntity entity);
    void setMoveTarget(BlockPos pos);
    LivingEntity getAttackTarget();
    BlockPos getMoveTarget();

    IBaritoneMoveControl getFortressMoveControl();
    void resetTargets();

    default double getTargetMoveRange() {
        return 2.0;
    }

}
