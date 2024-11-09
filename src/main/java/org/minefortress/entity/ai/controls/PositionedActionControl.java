package org.minefortress.entity.ai.controls;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import org.minefortress.entity.Colonist;

abstract class PositionedActionControl  {

    protected BlockPos goal;
    protected Item item;
    protected ITaskBlockInfo taskBlockInfo;

    public void set(ITaskBlockInfo taskBlockInfo) {
        this.taskBlockInfo = taskBlockInfo;
        this.goal = taskBlockInfo.getPos();
        this.item = taskBlockInfo.getPlacingItem();
    }

    public abstract void tick();

    public boolean isDone() {
        return goal == null;
    }

    public void reset() {
        this.goal = null;
        this.item = null;
    }

    protected boolean canReachTheGoal(Colonist colonsit) {
        return goal.isWithinDistance(colonsit.getBlockPos(), Colonist.WORK_REACH_DISTANCE+1) || colonsit.isAllowToPlaceBlockFromFarAway();
    }

}
