package org.minefortress.entity;

import net.minecraft.entity.LivingEntity;
import org.minefortress.entity.ai.controls.FightControl;

public interface IWarriorPawn extends IFortressAwareEntity {

    int getId();
    FightControl getFightControl();
    LivingEntity getTarget();

}
