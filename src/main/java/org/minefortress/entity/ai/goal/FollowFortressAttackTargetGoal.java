package org.minefortress.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import org.minefortress.entity.Colonist;

import java.util.EnumSet;

public class FollowFortressAttackTargetGoal extends Goal {

    private final Colonist colonist;
    private final float speed;
    private final float range;
    private LivingEntity target;

    private long stopTime;

    public FollowFortressAttackTargetGoal(Colonist colonist, float speed, float range) {
        this.colonist = colonist;
        this.speed = speed;
        this.range = range;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if(this.colonist.hasTask()) {
            return false;
        }

        final var target1 = this.colonist.getTarget();
        if(target1 != null && target1.isAlive() && this.colonist.squaredDistanceTo(target1) >= range * range) {
            this.target = target1;
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        return this.target.isAlive() &&
                !this.colonist.hasTask() &&
                this.colonist.squaredDistanceTo(this.target) >= range * range &&
                this.colonist.getFortressServerManager()
                        .map(it -> it.isPositionWithinFortress(colonist.getBlockPos()))
                        .orElse(false);
    }

    @Override
    public void start() {
        this.colonist.setAttacking(true);
        this.colonist.getMovementHelper().follow(this.target, this.speed);
    }

    @Override
    public void stop() {
        this.colonist.setAttacking(false);
        this.colonist.getMovementHelper().reset();
        this.target = null;
    }
}
