package org.minefortress.entity.ai;

import baritone.api.IBaritone;
import baritone.api.behavior.IPathingBehavior;
import baritone.api.event.events.BlockInteractEvent;
import baritone.api.event.events.PathEvent;
import baritone.api.event.listener.IGameEventListener;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovementHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovementHelper.class);

    private final Colonist colonist;
    private final IBaritone baritone;
    private BlockPos workGoal;

    private boolean stuck = false;

    public MovementHelper(Colonist colonist) {
        this.colonist = colonist;
        this.baritone = colonist.getBaritone();
        baritone.getGameEventHandler().registerEventListener(new StuckOnFailEventListener());
    }

    public void reset() {
        this.workGoal = null;
        this.stuck = false;
        this.baritone.getPathingBehavior().cancelEverything();
        this.colonist.setAllowToPlaceBlockFromFarAway(false);
    }

    public BlockPos getWorkGoal() {
        return workGoal;
    }

    public void set(BlockPos goal) {
        this.reset();
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
    }

    public boolean stillTryingToReachGoal() {
        return baritone.getPathingBehavior().isPathing();
    }

    public boolean isCantFindPath() {
        return stuck;
    }

    private class StuckOnFailEventListener implements IGameEventListener {

        @Override
        public void onTickServer() {

        }

        @Override
        public void onBlockInteract(BlockInteractEvent blockInteractEvent) {

        }

        @Override
        public void onPathEvent(PathEvent pathEvent) {
            if(pathEvent == PathEvent.CALC_FAILED) {
                MovementHelper.LOGGER.warn("Can't find path");
                MovementHelper.this.stuck = true;
            }
        }
    }

}
