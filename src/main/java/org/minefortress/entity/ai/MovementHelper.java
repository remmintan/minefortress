package org.minefortress.entity.ai;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;

public class MovementHelper {

    private final Colonist colonist;
    private final IBaritone baritone;
    private BlockPos workGoal;

    public MovementHelper(Colonist colonist) {
        this.colonist = colonist;
        this.baritone = colonist.getBaritone();
    }

    public void reset() {
        this.workGoal = null;
        this.baritone.getPathingBehavior().cancelEverything();
        this.colonist.setAllowToPlaceBlockFromFarAway(false);
    }

    public BlockPos getWorkGoal() {
        return workGoal;
    }

    public void set(BlockPos goal) {
        if(goal != null && goal.equals(workGoal))
            this.colonist.getNavigation().stop();

        this.workGoal = goal;
        this.colonist.setAllowToPlaceBlockFromFarAway(false);
        this.colonist.getNavigation().stop();
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(workGoal, (int)Colonist.WORK_REACH_DISTANCE-1));
    }

    public boolean hasReachedWorkGoal() {
        if(this.workGoal == null) return false;

        final boolean withinDistance =
                this.workGoal.isWithinDistance(this.colonist.getBlockPos(), Colonist.WORK_REACH_DISTANCE)
                || this.colonist.isAllowToPlaceBlockFromFarAway();

        return withinDistance && !baritone.getPathingBehavior().isPathing();
    }

    public void tick() {
        if(workGoal == null) return;
        if(!baritone.getPathingBehavior().isPathing() && !this.hasReachedWorkGoal())
            baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(workGoal, (int)Colonist.WORK_REACH_DISTANCE-1));
    }

    public boolean stillTryingToReachGoal() {
        return baritone.getPathingBehavior().isPathing();
    }

    public boolean isCantFindPath() {
        return baritone.getPathingBehavior().hasPath();
    }
}
