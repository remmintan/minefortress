package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.entity.ai.controls.TaskControl;
import org.minefortress.tasks.*;
import org.minefortress.entity.ai.ColonistNavigation;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.block.info.TaskBlockInfo;

import java.util.*;

public class ColonistExecuteTaskGoal extends Goal {

    private final Colonist colonist;
    private final ServerWorld world;

    private final MovementHelper movementHelper;

    private BlockPos workGoal =  null;

    @Override
    public boolean canStop() {
        return false;
    }

    public ColonistExecuteTaskGoal(Colonist colonist) {
        this.colonist = colonist;

        World level = this.colonist.world;
        if(level instanceof ServerWorld) {
            this.world = (ServerWorld) level;
        } else
            throw new IllegalStateException("AI should run on the server entities!");

        this.movementHelper = new MovementHelper((ColonistNavigation) this.colonist.getNavigation(), this.colonist);
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return getTaskControl().hasTask();
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

        if(this.movementHelper.hasReachedWorkGoal()) {
            boolean digSuccess = getTaskControl().is(TaskType.REMOVE) && colonist.getDigControl().isDone();
            boolean placeSuccess = getTaskControl().is(TaskType.BUILD) && colonist.getPlaceControl().isDone();
            if(digSuccess || placeSuccess) {
                moveToNextBlock();
            }
        }

        this.movementHelper.tick();
        if(this.movementHelper.isCantFindPath() || this.colonist.getPlaceControl().isCantPlaceUnderMyself()) {
            getTaskControl().fail();
            this.colonist.resetControls();
        }
    }

    @Override
    public boolean shouldContinue() {
        return getTaskControl().hasTask() &&
            (
                movementHelper.stillTryingToReachGoal() ||
                workGoal !=null ||
                !getTaskControl().finished() ||
                colonist.diggingOrPlacing()
            );
    }

    @Override
    public void stop() {
        getTaskControl().success();
        this.movementHelper.reset();
        this.colonist.resetControls();
        this.workGoal = null;
    }

    private void moveToNextBlock() {
        this.movementHelper.reset();
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
        movementHelper.set(workGoal);
        colonist.setGoal(taskBlockInfo);
    }

    private boolean blockInCorrectState(BlockPos pos) {
        if(pos == null) return false;
        if(getTaskControl().is(TaskType.REMOVE)) {
            return BuildingManager.canRemoveBlock(world, pos);
        } else if(getTaskControl().is(TaskType.BUILD)) {
            return BuildingManager.canPlaceBlock(world, pos);
        } else {
            throw new IllegalStateException();
        }
    }

    private TaskControl getTaskControl() {
        return this.colonist.getTaskControl();
    }
}
