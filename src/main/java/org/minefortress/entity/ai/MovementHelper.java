package org.minefortress.entity.ai;

import baritone.api.IBaritone;
import baritone.api.event.events.PathEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.utils.BetterBlockPos;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovementHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovementHelper.class);

    private final Colonist colonist;
    private final IBaritone baritone;
    private BlockPos workGoal;

    private int stuckTicks = 0;
    private boolean stuck = false;
    private BlockPos lastPos = null;

    public MovementHelper(Colonist colonist) {
        this.colonist = colonist;
        this.baritone = colonist.getBaritone();
        baritone.getGameEventHandler().registerEventListener(new StuckOnFailEventListener());
    }

    public void reset() {
        final var hasWorkGoal = workGoal != null;
        final var tryingToReachGoal = stillTryingToReachGoal();
        LOGGER.info("{} movement helper reset [has work goal: {}, trying to reach the goal {}]", getColonistName(), hasWorkGoal, tryingToReachGoal);
        this.workGoal = null;
        this.lastPos = null;
        this.stuckTicks = 0;
        this.stuck = false;
        this.baritone.getPathingBehavior().cancelEverything();
        this.colonist.setAllowToPlaceBlockFromFarAway(false);
    }

    private String getColonistName() {
        return colonist.getName().asString();
    }

    public BlockPos getWorkGoal() {
        return workGoal;
    }

    public void set(BlockPos goal, float speed) {
        if(workGoal != null && workGoal.equals(goal)){
            LOGGER.info("{} trying to set new goal, but current goal is the same", getColonistName());
            return;
        }
        LOGGER.info("{} set new goal {}. speed: {}", getColonistName(), goal, speed);
        this.reset();
        this.workGoal = goal;
        this.colonist.setAllowToPlaceBlockFromFarAway(false);
        this.colonist.setMovementSpeed(speed);
        this.colonist.getNavigation().stop();
        if(this.hasReachedWorkGoal()){
            LOGGER.info("{} the goal {} is already reached", getColonistName(), goal);
            return;
        }
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

        final var currentPos = colonist.getBlockPos();
        if(!hasReachedWorkGoal() && currentPos.equals(lastPos)) {
            stuckTicks++;
            LOGGER.info("{} on the same place without reaching the goal for {} ticks. Goal: {}", getColonistName(), stuckTicks, workGoal);
            if(stuckTicks > 20) {
                LOGGER.info("{} on the same place for too long. Setting stuck to true. Goal: {}", getColonistName(), workGoal);
                stuck = true;
                stuckTicks = 0;
            }
        } else {
            stuck = false;
            stuckTicks = 0;
        }
        lastPos = currentPos;
    }

    public boolean stillTryingToReachGoal() {
        return baritone.getPathingBehavior().isPathing();
    }

    public boolean isStuck() {
        return stuck;
    }

    private class StuckOnFailEventListener implements AbstractGameEventListener {

        private BlockPos lastDestination;
        private int stuckCounter = 0;

        @Override
        public void onPathEvent(PathEvent pathEvent) {
            if(pathEvent == PathEvent.AT_GOAL && !hasReachedWorkGoal()) {
                LOGGER.info("{} signaling at goal without actually reaching the goal {}. Setting stuck to true", getColonistName(), workGoal);
                stuck = true;
            }

            if(pathEvent == PathEvent.CALC_FINISHED_NOW_EXECUTING){
                final var dest = baritone.getPathingBehavior().getPath().map(IPath::getDest).orElse(BetterBlockPos.ORIGIN);
                if(lastDestination != null) {
                    if (dest.equals(lastDestination)) {
                        stuckCounter++;
                        LOGGER.info("{} Calculated destination is the same as previous for {} ticks (going in circles). [Goal: {}]", getColonistName(), stuckCounter, workGoal);
                        if (stuckCounter > 1) {
                            LOGGER.info("{} going in circles for too much time {} [goal: {}]", getColonistName(), stuckCounter, workGoal);
                            stuck = true;
                            stuckCounter = 0;
                            lastDestination = null;
                            baritone.getPathingBehavior().cancelEverything();
                        }
                    } else {
                        stuckCounter = 0;
                    }
                }
                lastDestination = dest;
            }

            if(pathEvent == PathEvent.CALC_FAILED) {
                MovementHelper.LOGGER.warn("{} can't find path to {}", getColonistName(), workGoal);
                MovementHelper.this.stuck = true;
            }
        }
    }

}
