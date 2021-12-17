package org.minefortress.entity.ai.controls;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;

abstract class PositionedActionControl  {

    protected BlockPos goal;
    protected Item item;

    public void set(BlockPos pos, Item item) {
        this.goal = pos.toImmutable();
        this.item = item;
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

    protected boolean canReachTheGoal(Entity entity) {
        return goal.isWithinDistance(entity.getBlockPos().up(), Colonist.WORK_REACH_DISTANCE);
    }

}
