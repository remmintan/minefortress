package org.minefortress.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public final class SelectTargetToAttackGoal extends TrackTargetGoal {

    private final static Predicate<LivingEntity> ALWAYS_TRUE = livingEntity -> true;
    private final Class<LivingEntity> targetClass = LivingEntity.class;
    /**
     * The reciprocal of chance to actually search for a target on every tick
     * when this goal is not started. This is also the average number of ticks
     * between each search (as in a poisson distribution).
     */
    private final int reciprocalChance;
    @Nullable
    private LivingEntity targetEntity;
    private final TargetPredicate targetPredicate;

    public SelectTargetToAttackGoal(MobEntity mob, @NotNull Predicate<LivingEntity> targetPredicate) {
        super(mob, false, true);
        this.reciprocalChance = ActiveTargetGoal.toGoalTicks(0);
        this.setControls(EnumSet.of(Control.TARGET));
        this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(targetPredicate);
    }

    @Override
    public boolean canStart() {
        if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
            return false;
        }
        this.findClosestTarget();
        return this.targetEntity != null;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetEntity);
        super.start();
    }

    private Box getSearchBox(double distance) {
        return this.mob.getBoundingBox().expand(distance, 4.0, distance);
    }

    private void findClosestTarget() {
        final var searchBox = this.getSearchBox(this.getFollowRange());
        final var targetEntitiesNearYou = this.mob.getWorld().getEntitiesByClass(this.targetClass, searchBox, ALWAYS_TRUE);
        this.targetEntity = this.mob.getWorld().getClosestEntity(targetEntitiesNearYou, this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
    }

}
