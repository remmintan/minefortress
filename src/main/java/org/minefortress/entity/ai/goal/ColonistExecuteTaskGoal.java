package org.minefortress.entity.ai.goal;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        World level = this.colonist.world;
        if(level instanceof ServerWorld) {
            this.world = (ServerWorld) level;
        } else {
            throw new IllegalStateException("AI should run on the server entities!");
        }

    }

    @Override
    public boolean canStop() {
        return super.isStarving() || super.isScared() || super.isFighting() || super.isHiding();
    }

    @Override
    public boolean canStart() {
        final var notInCombat = notInCombat();
        final var hasTask = getTaskControl().hasTask();
        final var notStarving = !super.isStarving();
        LOGGER.debug("{} can executeTask [not in combat: {}, has task: {}, not starving: {}]", getColonistName(), notInCombat, hasTask, notStarving);
        return notInCombat && hasTask && notStarving;
    }

    private String getColonistName() {
        return colonist.getName().asString();
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
            getMovementHelper().set(workGoal, Colonist.FAST_MOVEMENT_SPEED);
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
        final var notInCombat = notInCombat();
        final var notStarving = !super.isStarving();
        final var hasTask = getTaskControl().hasTask();
        final var hasGoalTryingToReachOrWorking = getMovementHelper().stillTryingToReachGoal() ||
                workGoal != null ||
                !getTaskControl().finished() ||
                colonist.diggingOrPlacing();
        final var shouldContinue = notInCombat && notStarving && hasTask && hasGoalTryingToReachOrWorking;
        LOGGER.debug("{} should continue task execution {} [not in combat {}, not starving {}, has task {}, has goal and working {}, digging or placing {}]", getColonistName(), shouldContinue, notInCombat, notStarving, hasTask, hasGoalTryingToReachOrWorking, colonist.diggingOrPlacing());
        return shouldContinue;
    }

    @Override
    public void stop() {
        LOGGER.debug("{} stopping the task execution", getColonistName());
        if(!notInCombat()) {
            final var idOpt = getTaskControl().getTaskId();
            if(idOpt.isPresent()) {
                final var id = idOpt.get();
                LOGGER.debug("{} stopping task execution because of combat. Return reserved items for task {}", getColonistName(), id);
                colonist
                        .getFortressServerManager()
                        .orElseThrow()
                        .getServerResourceManager()
                        .returnReservedItems(id);
            } else {
                LOGGER.debug("{} stopping task execution because of combat. No task id found", getColonistName());
            }
            getTaskControl().fail();
        }
        if(getTaskControl().hasTask()) {
            LOGGER.debug("{} finishing task successfully", getColonistName());
            getTaskControl().success();
        }
        this.colonist.resetControls();
        this.workGoal = null;
    }

    private void moveToNextBlock() {
        LOGGER.debug("{} moving to next block", getColonistName());
        getMovementHelper().reset();
        workGoal = null;
        TaskBlockInfo taskBlockInfo = null;
        while (!getTaskControl().finished()) {
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
        getMovementHelper().set(workGoal, Colonist.FAST_MOVEMENT_SPEED);
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
