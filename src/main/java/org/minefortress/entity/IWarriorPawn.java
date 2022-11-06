package org.minefortress.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public interface IWarriorPawn extends IFortressAwareEntity {

    int getId();
    LivingEntity getTarget();

    void setAttackTarget(LivingEntity entity);
    void setMoveTarget(BlockPos pos);
    LivingEntity getAttackTarget();
    BlockPos getMoveTarget();

}
