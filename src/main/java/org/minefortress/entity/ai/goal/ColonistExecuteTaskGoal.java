package org.minefortress.entity.ai.goal;


import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.ITaskControl;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;

import static net.remmintan.mods.minefortress.core.ModLogger.LOGGER;

public class ColonistExecuteTaskGoal extends AbstractFortressGoal {
    private final ServerWorld world;
    private BlockPos workGoal =  null;

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
        LOGGER.debug("{} can executeTask [ has task: {}, not starving: {}]", getColonistName(), hasTask, notStarving);
        return  hasTask && notStarving;
    }


    @Override
    public void start() {
        LOGGER.debug("{} start executing task", getColonistName());
        if(colonist.isSleeping()) {
            colonist.wakeUp();
        }
        moveToNextBlock();
    }

    @Override
    public void tick() {
        if(this.workGoal == null) {
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

        if(getMovementHelper().getWorkGoal() == null) {
            getMovementHelper().goTo(workGoal, Colonist.FAST_MOVEMENT_SPEED);
        }

        if(getMovementHelper().hasReachedWorkGoal()) {
            LOGGER.debug("{} reached work goal {} working", getColonistName(), workGoal);
            boolean digSuccess = getTaskControl().is(TaskType.REMOVE) && colonist.getDigControl().isDone();
            boolean placeSuccess = getTaskControl().is(TaskType.BUILD) && colonist.getPlaceControl().isDone();
            if(digSuccess || placeSuccess) {
                LOGGER.debug("{} action successful moving to next block [digSuccess {}, placeSuccess {}]", getColonistName(), digSuccess, placeSuccess);
                moveToNextBlock();
            }
        }

        final var movementHelperStuck = !getMovementHelper().hasReachedWorkGoal() && getMovementHelper().isStuck();
        final var cantPlaceUnderMyself = this.colonist.getPlaceControl().isCantPlaceUnderMyself();
        if(movementHelperStuck || cantPlaceUnderMyself) {
            LOGGER.debug("{} stuck with moving or placing failing task [movement helper stuck: {}, cant place under myself: {}]", getColonistName(), movementHelperStuck, cantPlaceUnderMyself);
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
        LOGGER.debug("{} stopping the task execution", getColonistName());
        final var taskControl = getTaskControl();
        if(taskControl.hasTaskPart()) {
            if(taskControl.partHasMoreBlocks()) {
                LOGGER.debug("{} failing task part", getColonistName());
               taskControl.fail();
            } else {
                LOGGER.debug("{} finishing task successfully", getColonistName());
                taskControl.success();
            }
        }
        this.colonist.resetControls();
        this.workGoal = null;
    }

    private void moveToNextBlock() {
        LOGGER.debug("{} moving to next block", getColonistName());
        getMovementHelper().reset();
        workGoal = null;
        ITaskBlockInfo taskBlockInfo = null;
        while (getTaskControl().partHasMoreBlocks()) {
            LOGGER.debug("{} task is not finished yet", getColonistName());
            taskBlockInfo = getTaskControl().getNextBlock();
            if(taskBlockInfo == null){
                LOGGER.debug("{} next block is null. skipping", getColonistName());
                continue;
            }
            workGoal = taskBlockInfo.getPos();
            LOGGER.debug("{} set up new move goal {}", getColonistName(), workGoal);
            if(blockInCorrectState(workGoal)){
                LOGGER.debug("{} block in in correct state, selecting it {}", getColonistName(), workGoal);
                break; // skipping air blocks
            } else {
                LOGGER.debug("{} block is not in correct state, skipping it {}", getColonistName(), workGoal);
            }
        }
        if(!blockInCorrectState(workGoal)){
            LOGGER.debug("{} task is finished and the last block is not in correct state {}", getColonistName(), workGoal);
            this.workGoal = null;
        }

        if(workGoal == null || taskBlockInfo == null){
            LOGGER.debug("{} work goal [{}] or task block info [{}] is not set", getColonistName(), workGoal, taskBlockInfo);
            return;
        }
        LOGGER.debug("{} setting work goal {}", getColonistName(), taskBlockInfo);
        getMovementHelper().goTo(workGoal, Colonist.FAST_MOVEMENT_SPEED);
        LOGGER.debug("{} setting goal for colonist {}", getColonistName(), taskBlockInfo);
        colonist.setGoal(taskBlockInfo);
    }

    private boolean blockInCorrectState(BlockPos pos) {
        if(pos == null) return false;
        if(getTaskControl().is(TaskType.REMOVE)) {
            if (colonist.getFortressPos().equals(pos)) return false;
            return BuildingHelper.canRemoveBlock(world, pos);
        } else if(getTaskControl().is(TaskType.BUILD)) {
            return BuildingHelper.canPlaceBlock(world, pos);
        } else {
            throw new IllegalStateException();
        }
    }

    private ITaskControl getTaskControl() {
        return this.colonist.getTaskControl();
    }

    private MovementHelper getMovementHelper() {
        return this.colonist.getMovementHelper();
    }
}
