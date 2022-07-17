package org.minefortress.entity.ai.controls;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.block.info.TaskBlockInfo;

abstract class PositionedActionControl  {

    protected BlockPos goal;
    protected Item item;
    protected TaskBlockInfo taskBlockInfo;

    public void set(TaskBlockInfo taskBlockInfo) {
        this.taskBlockInfo = taskBlockInfo;
        this.goal = taskBlockInfo.getPos();
        this.item = taskBlockInfo.getPlacingItem();
    }

    public abstract void tick();

    public boolean isWorking() {
        return goal != null;
    }

    public boolean isDone() {
        return goal == null;
    }

    public void reset() {
        this.goal = null;
        this.item = null;
    }

    protected boolean canReachTheGoal(Colonist colonsit) {
        return goal.isWithinDistance(colonsit.getBlockPos(), Colonist.WORK_REACH_DISTANCE) || colonsit.isAllowToPlaceBlockFromFarAway();
    }

}
