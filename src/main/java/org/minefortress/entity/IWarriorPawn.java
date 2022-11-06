package org.minefortress.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.minefortress.entity.ai.controls.FighterMoveControl;

public interface IWarriorPawn extends IFortressAwareEntity, IBaritonableEntity {

    int getId();
    Vec3d getPos();
    LivingEntity getTarget();

    void setAttackTarget(LivingEntity entity);
    void setMoveTarget(BlockPos pos);
    LivingEntity getAttackTarget();
    BlockPos getMoveTarget();

    FighterMoveControl getFighterMoveControl();

}
