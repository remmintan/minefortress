package org.minefortress.entity.ai.goal;


import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.entity.ai.controls.TaskControl;
import org.minefortress.tasks.TaskType;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.utils.BuildingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColonistExecuteTaskGoal extends AbstractFortressGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColonistExecuteTaskGoal.class);

    private final ServerWorld world;

    private BlockPos workGoal =  null;

    public ColonistExecuteTaskGoal(Colonist colonist) {
        super(colonist);
        if(colonist.world instanceof ServerWorld sw) {
            this.world = sw;
        } else {
            throw new IllegalStateException("AI should run on the server entities!");
        }
    }

    @Override
    public boolean canStart() {
        final var hasTask = getTaskControl().hasTask();
        final var notStarving = !super.isStarving();
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
        final var hasTask = getTaskControl().hasTask();
        if(this.workGoal == null || !hasTask){
            LOGGER.debug("{} don't have any work, but keep ticking [work goal {}, has task {}]", getColonistName(), workGoal, hasTask);
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

        final var movementHelperStuck = getMovementHelper().isStuck();
        final var cantPlaceUnderMyself = this.colonist.getPlaceControl().isCantPlaceUnderMyself();
        if(movementHelperStuck || cantPlaceUnderMyself) {
            LOGGER.debug("{} stuck with moving or placing failing task [movement helper stuck: {}, cant place under myself: {}]", getColonistName(), movementHelperStuck, cantPlaceUnderMyself);
            getTaskControl().fail();
            this.colonist.resetControls();
        }
    }

    @Override
    public boolean shouldContinue() {
        final var notStarving = !super.isStarving();
        final var hasTask = getTaskControl().hasTask();
        final var hasGoalTryingToReachOrWorking = getMovementHelper().stillTryingToReachGoal() ||
                workGoal != null ||
                getTaskControl().partHasMoreBlocks() ||
                colonist.diggingOrPlacing();
        final var shouldContinue = notStarving && hasTask && hasGoalTryingToReachOrWorking;
        LOGGER.debug("{} should continue task execution {} [not starving {}, has task {}, has goal and working {}, digging or placing {}]", getColonistName(), shouldContinue, notStarving, hasTask, hasGoalTryingToReachOrWorking, colonist.diggingOrPlacing());
        return shouldContinue;
    }

    @Override
    public void stop() {
        LOGGER.debug("{} stopping the task execution", getColonistName());
        final var taskControl = getTaskControl();
        if(taskControl.hasTask()) {
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
        TaskBlockInfo taskBlockInfo = null;
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
            if(pos.equals(colonist.getFortressServerManager().orElseThrow().getFortressCenter())) return false;
            return BuildingHelper.canRemoveBlock(world, pos);
        } else if(getTaskControl().is(TaskType.BUILD)) {
            return BuildingHelper.canPlaceBlock(world, pos);
        } else {
            throw new IllegalStateException();
        }
    }

    private TaskControl getTaskControl() {
        return this.colonist.getTaskControl();
    }

    private MovementHelper getMovementHelper() {
        return this.colonist.getMovementHelper();
    }
}
