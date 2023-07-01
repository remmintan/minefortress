package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import org.minefortress.entity.Colonist;

public class FortressColonistActiveTargetGoal extends ActiveTargetGoal<HostileEntity> {
    private final Colonist colonist;
    public FortressColonistActiveTargetGoal(Colonist mob) {
        super(
                mob,
                HostileEntity.class,
                false,
                entity -> !(entity instanceof CreeperEntity) && !(entity instanceof EndermanEntity)
        );
        this.colonist = mob;
    }

    @Override
    public boolean canStart() {
        if(this.colonist.isSleeping()) return false;
        if(!super.canStart()) {
            return false;
        }

        if(this.targetEntity != null) {
            final var fortressServerManagerOpt = this.colonist.getFortressServerManager();
            if(fortressServerManagerOpt.isPresent()) {
                final var serverManager = fortressServerManagerOpt.get();
                final var targetWithinFortress = serverManager.isPositionWithinFortress(this.targetEntity.getBlockPos());
                if(targetWithinFortress) {
                    return true;
                }
            }
        }

        this.targetEntity = null;
        return false;
    }
}
