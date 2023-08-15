package org.minefortress.entity.ai.controls;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.PathEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.utils.BetterBlockPos;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.BaritonableEntity;
import org.minefortress.entity.TargetedPawn;
import org.minefortress.entity.WarriorPawn;
import org.minefortress.entity.interfaces.IFortressAwareEntity;
import org.minefortress.entity.interfaces.ITargetedPawn;
import org.minefortress.entity.interfaces.IWarrior;

import java.util.Optional;

public class BaritoneMoveControl {

    private final IBaritone baritone;
    private final BaritonableEntity entity;

    private BlockPos moveTarget;
    private LivingEntity followTarget;

    private boolean stuck = false;
    private double currentReachRange = 0;

    public BaritoneMoveControl(BaritonableEntity entity) {
        this.entity = entity;
        this.baritone = BaritoneAPI.getProvider().getBaritone(entity);
        this.baritone.getGameEventHandler().registerEventListener(new StuckOnFailEventListener());
    }

    public void moveTo(@NotNull BlockPos pos) {
        this.reset(true);
        this.updateReachRange(false);
        this.entity.setMovementSpeed((float)this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        this.moveTarget = pos;
        final var goal = new GoalNear(pos, (int) Math.floor(currentReachRange));
        baritone.getCustomGoalProcess().setGoalAndPath(goal);
    }

    public void moveTo(@NotNull LivingEntity entity) {
        this.reset(true);
        this.updateReachRange(true);
        this.baritone.settings().followRadius.set((int)Math.floor(currentReachRange));
        this.followTarget = entity;
        this.entity.setMovementSpeed((float)this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
        baritone.getFollowProcess().follow(it -> it.equals(entity));
    }

    public void reset() {
        reset(false);
    }

    private void reset(boolean startMoving) {
        if(startMoving && entity.isSleeping()) {
            entity.wakeUp();
        }
        baritone.getFollowProcess().cancel();
        baritone.getPathingBehavior().cancelEverything();
        moveTarget = null;
        followTarget = null;
        stuck = false;
        currentReachRange = 0;
    }

    public boolean isStuck() {
        return stuck;
    }

    private Optional<BlockPos> getTargetPos() {
        return Optional.ofNullable(moveTarget).or(() -> Optional.ofNullable(followTarget).map(LivingEntity::getBlockPos));
    }

    private boolean moveTargetInRange() {
        return getTargetPos().map(it -> it.isWithinDistance(entity.getPos(), currentReachRange)).orElse(false);
    }

    private void updateReachRange(boolean follow) {
        if(follow) {
            if(entity instanceof IWarrior warrior) {
                currentReachRange = warrior.getAttackRange();
                return;
            }
        }

        if(entity instanceof ITargetedPawn targeted) {
            currentReachRange = targeted.getTargetMoveRange();
            return;
        }

        currentReachRange = entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
    }

    private class StuckOnFailEventListener implements AbstractGameEventListener {

        private BlockPos lastDestination;
        private int stuckCounter = 0;

        @Override
        public void onPathEvent(PathEvent pathEvent) {
            checkFalseAtGoal(pathEvent);
            checkStuckOnTheSamePlace(pathEvent);
            checkFailedToCalc(pathEvent);
        }

        private void checkFalseAtGoal(PathEvent pathEvent) {
            if(pathEvent == PathEvent.AT_GOAL && !moveTargetInRange()) {
                stuck = true;
            }
        }

        private void checkFailedToCalc(PathEvent pathEvent) {
            if(pathEvent == PathEvent.CALC_FAILED) {
                if(entity instanceof IFortressAwareEntity fae && entity instanceof TargetedPawn targetedPawn)  {
                    fae.sendMessageToMasterPlayer(getPawnType(fae) + " " + entity.getName().getContent() + " can't reach the target");
                    targetedPawn.resetTargets();
                }
                stuck = true;
            }
        }

        private static String getPawnType(IFortressAwareEntity fae) {
            if(fae instanceof WarriorPawn) {
                return "Warrior";
            }
            return "Pawn";
        }

        private void checkStuckOnTheSamePlace(PathEvent pathEvent) {
            if(pathEvent == PathEvent.CALC_FINISHED_NOW_EXECUTING){
                final var dest = baritone.getPathingBehavior().getPath().map(IPath::getDest).orElse(BetterBlockPos.ORIGIN);
                if(lastDestination != null) {
                    if (!BlockPos.ORIGIN.equals(lastDestination) && dest.equals(lastDestination)) {
                        stuckCounter++;
                        if (stuckCounter > 1) {
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
        }
    }

}
