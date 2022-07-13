package org.minefortress.entity.ai.goal;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.entity.ai.controls.TaskControl;
import org.minefortress.utils.BuildingHelper;
import org.minefortress.tasks.TaskType;
import org.minefortress.tasks.block.info.TaskBlockInfo;

public class ColonistExecuteTaskGoal extends AbstractFortressGoal {

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
        return notInCombat() && getTaskControl().hasTask() && !super.isStarving();
    }

    @Override
    public void start() {
        if(colonist.isSleeping()) {
            colonist.wakeUp();
        }
        moveToNextBlock();
    }

    @Override
    public void tick() {
        if(this.workGoal == null || !getTaskControl().hasTask()) return;

        if(getMovementHelper().hasReachedWorkGoal()) {
            boolean digSuccess = getTaskControl().is(TaskType.REMOVE) && colonist.getDigControl().isDone();
            boolean placeSuccess = getTaskControl().is(TaskType.BUILD) && colonist.getPlaceControl().isDone();
            if(digSuccess || placeSuccess) {
                moveToNextBlock();
            }
        }

        getMovementHelper().tick();
        if(getMovementHelper().isCantFindPath() || this.colonist.getPlaceControl().isCantPlaceUnderMyself()) {
            getTaskControl().fail();
            this.colonist.resetControls();
        }
    }

    @Override
    public boolean shouldContinue() {
        return notInCombat() && !super.isStarving() && getTaskControl().hasTask() &&
            (
                getMovementHelper().stillTryingToReachGoal() ||
                workGoal !=null ||
                !getTaskControl().finished() ||
                colonist.diggingOrPlacing()
            );
    }

    @Override
    public void stop() {
        getTaskControl().success();
        getMovementHelper().reset();
        this.colonist.resetControls();
        this.workGoal = null;
    }

    private void moveToNextBlock() {
        getMovementHelper().reset();
        workGoal = null;
        TaskBlockInfo taskBlockInfo = null;
        while (!getTaskControl().finished()) {
            taskBlockInfo = getTaskControl().getNextBlock();
            if(taskBlockInfo == null) continue;
            workGoal = taskBlockInfo.getPos();
            if(blockInCorrectState(workGoal)) break; // skipping air blocks
        }
        if(!blockInCorrectState(workGoal)){
            this.workGoal = null;
        }

        if(workGoal == null || taskBlockInfo == null) return;
        getMovementHelper().set(workGoal);
        colonist.setGoal(taskBlockInfo);
    }

    private boolean blockInCorrectState(BlockPos pos) {
        if(pos == null) return false;
        if(getTaskControl().is(TaskType.REMOVE)) {
            if(colonist.getFortressServerManager().getFortressCenter().equals(pos)) return false;
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
