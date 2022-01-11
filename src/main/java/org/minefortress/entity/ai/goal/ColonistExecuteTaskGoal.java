package org.minefortress.entity.ai.goal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.tasks.*;
import org.minefortress.entity.ai.ColonistNavigation;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.entity.Colonist;
import org.minefortress.interfaces.FortressServerWorld;
import org.minefortress.tasks.block.info.TaskBlockInfo;
import org.minefortress.tasks.interfaces.Task;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ColonistExecuteTaskGoal extends Goal {

    private final Colonist colonist;
    private final ServerWorld world;
    private final TaskManager manager;

    private final MovementHelper movementHelper;

    private Task task;
    private TaskPart part;
    private Iterator<TaskBlockInfo> currentPartBlocksIterator;

    private TaskBlockInfo taskBlockInfo = null;
    private BlockPos nextBlock =  null;

    private final Cache<UUID, Object> returnedIds = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    @Override
    public boolean canStop() {
        return false;
    }

    public ColonistExecuteTaskGoal(Colonist colonist) {
        this.colonist = colonist;

        World level = this.colonist.world;
        if(level instanceof ServerWorld) {
            this.world = (ServerWorld) level;
            this.manager = ((FortressServerWorld)world).getTaskManager();
        } else
            throw new IllegalStateException("AI should run on the server entities!");

        this.movementHelper = new MovementHelper((ColonistNavigation) this.colonist.getNavigation(), this.colonist);
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        Set<UUID> returnedIds = this.returnedIds.asMap().keySet();
        return manager.hasTask() && manager.nextTaskIdIsNotIn(returnedIds);
    }

    @Override
    public void start() {
        colonist.setHasTask(true);
        task = manager.getTask();
        part = task.getNextPart(world);
        currentPartBlocksIterator = part.getIterator();

        if(!task.hasAvailableParts())
            manager.removeTask();

        moveToNextBlock();
    }

    @Override
    public void tick() {
        if(this.nextBlock == null || manager.isCancelled(task.getId())) return;

        if(this.movementHelper.hasReachedWorkGoal()) {
            boolean digSuccess = task.getTaskType() == TaskType.REMOVE && colonist.getDigControl().isDone();
            boolean placeSuccess = task.getTaskType() == TaskType.BUILD && colonist.getPlaceControl().isDone();
            if(digSuccess || placeSuccess) {
                moveToNextBlock();
            }
        }

        this.movementHelper.tick();
        if(this.movementHelper.isCantFindPath() || this.colonist.getPlaceControl().isCantPlaceUnderMyself()) {
            returnTask();
            this.colonist.resetControls();
            this.task = null;
        }
    }

    public void returnTask() {
        if(part == null) return;

        manager.returnTask(part);
        this.returnedIds.put(part.getTask().getId(), new Object());
        part = null;
    }

    @Override
    public boolean shouldContinue() {
        return this.task != null &&
            (
                movementHelper.stillTryingToReachGoal() ||
                nextBlock!=null ||
                currentPartBlocksIterator.hasNext() ||
                colonist.diggingOrPlacing()
            ) &&
                !manager.isCancelled(task.getId());
    }

    @Override
    public void stop() {
        colonist.setHasTask(false);
        this.movementHelper.reset();
        this.colonist.resetControls();

        if(this.task != null) {
            this.task.finishPart(world);
        }

        this.nextBlock = null;
        this.currentPartBlocksIterator = null;
        this.task = null;
        this.part = null;
    }

    private void moveToNextBlock() {
        this.movementHelper.reset();
        this.nextBlock = null;
        while (currentPartBlocksIterator.hasNext()) {
            taskBlockInfo = currentPartBlocksIterator.next();
            nextBlock = taskBlockInfo.getPos();
            if(currentBlockInCorrectState()) break; // skipping air blocks
        }
        if(!currentBlockInCorrectState()){
            this.nextBlock = null;
        }

        if(nextBlock == null) return;
        movementHelper.set(this.nextBlock);
        colonist.setGoal(this.taskBlockInfo);
    }

    private boolean currentBlockInCorrectState() {
        if(nextBlock == null) return false;
        if(task.getTaskType() == TaskType.REMOVE) {
            return BuildingManager.canRemoveBlock(world, nextBlock);
        } else if(task.getTaskType() == TaskType.BUILD) {
            return BuildingManager.canPlaceBlock(world, nextBlock);
        } else {
            throw new IllegalStateException();
        }
    }
}
