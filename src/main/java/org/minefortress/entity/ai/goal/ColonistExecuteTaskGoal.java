package org.minefortress.entity.ai.goal;


import net.minecraft.server.world.ServerWorld;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.ITaskControl;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.tasks.CutTreesTask;

public class ColonistExecuteTaskGoal extends AbstractFortressGoal {
    private final ServerWorld world;
    private ITaskBlockInfo goal;

    public ColonistExecuteTaskGoal(Colonist colonist) {
        super(colonist);
        if(colonist.getWorld() instanceof ServerWorld sw) {
            this.world = sw;
        } else {
            throw new IllegalStateException("AI should run on the server entities!");
        }
    }

    @Override
    public boolean canStart() {
        final var hasTask = getTaskControl().hasTask();
        final var notStarving = !super.isHungry();
        return  hasTask && notStarving;
    }


    @Override
    public void start() {
        if(colonist.isSleeping()) {
            colonist.wakeUp();
        }
        if (getTaskControl().taskIsOfType(CutTreesTask.class)) {
            colonist.setAllowToPlaceBlockFromFarAway(true);
        }
        moveToNextBlock();
    }

    @Override
    public void tick() {
        if (this.goal == null) {
            // quite tricky part
            // first of all we need to understand that if we have landed in this block we were not able to find
            // any block to work on in the last moveToNextBlock() call
            if(getTaskControl().hasTaskPart()) {
                // if we end up here that means that we still have some task part to work on
                // if we have more blocks in the current task part we should try to find the next one
                // otherwise we should finish the task part successfully
                if(getTaskControl().partHasMoreBlocks()) {
                    moveToNextBlock();
                } else  {
                    getTaskControl().success();
                }
            } else {
                getTaskControl().findNextPart();
            }

            return;
        }

        if (getMovementHelper().getGoal() == null) {
            var distance = getTaskControl().taskIsOfType(CutTreesTask.class) ? 6f : Colonist.WORK_REACH_DISTANCE;
            getMovementHelper().goTo(goal.getPos(), Colonist.FAST_MOVEMENT_SPEED, distance);
        }

        if (getMovementHelper().hasReachedGoal()) {
            boolean digSuccess = goal.getType() == TaskType.REMOVE && colonist.getDigControl().isDone();
            boolean placeSuccess = goal.getType() == TaskType.BUILD && colonist.getPlaceControl().isDone();
            if(digSuccess || placeSuccess) {
                moveToNextBlock();
            }
        }

        final var movementHelperStuck = !getMovementHelper().hasReachedGoal() && getMovementHelper().isStuck();
        final var cantPlaceUnderMyself = this.colonist.getPlaceControl().isCantPlaceUnderMyself();
        if(movementHelperStuck || cantPlaceUnderMyself) {
            getTaskControl().fail();
            this.colonist.resetControls();
        }
    }

    @Override
    public boolean shouldContinue() {
        return getTaskControl().hasTask() && !super.isHungry();
    }

    @Override
    public void stop() {
        final var taskControl = getTaskControl();
        if(taskControl.hasTaskPart()) {
            if(taskControl.partHasMoreBlocks()) {
               taskControl.fail();
            } else {
                taskControl.success();
            }
        }
        this.colonist.setAllowToPlaceBlockFromFarAway(false);
        this.colonist.resetControls();
        this.goal = null;
    }

    private void moveToNextBlock() {
        getMovementHelper().reset();

        while (getTaskControl().partHasMoreBlocks()) {
            goal = getTaskControl().getNextBlock();
            if (goal == null) {
                continue;
            }
            if (goal.isInCorrectState(world)) {
                break; // skipping air blocks
            }
        }
        if (goal != null && !goal.isInCorrectState(world)) {
            goal = null;
        }

        if (goal == null) {
            return;
        }
        getMovementHelper().goTo(goal.getPos(), Colonist.FAST_MOVEMENT_SPEED);
        colonist.setGoal(goal);
    }
    private ITaskControl getTaskControl() {
        return this.colonist.getTaskControl();
    }

    private MovementHelper getMovementHelper() {
        return this.colonist.getMovementHelper();
    }
}
