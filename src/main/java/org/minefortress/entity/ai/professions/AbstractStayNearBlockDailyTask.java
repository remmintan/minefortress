package org.minefortress.entity.ai.professions;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;

abstract class AbstractStayNearBlockDailyTask implements ProfessionDailyTask {


    private BlockPos blockPos;
    private int workingTicks;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.world.isDay();
    }

    @Override
    public void start(Colonist colonist) {
        this.setupTablePos(colonist);
        colonist.getMovementHelper().set(this.blockPos);
    }

    @Override
    public void tick(Colonist colonist) {
        if(this.blockPos == null) return;
        final MovementHelper movementHelper = colonist.getMovementHelper();
        if(movementHelper.hasReachedWorkGoal()) {
            if(workingTicks % 10 * colonist.getHungerMultiplier() == 0) {
                colonist.swingHand(colonist.world.random.nextFloat() < 0.5F? Hand.MAIN_HAND : Hand.OFF_HAND);
                colonist.putItemInHand(getWorkingItem());
                colonist.addExhaustion(0.005f);
            }
            colonist.lookAt(blockPos);
            workingTicks++;
        }
        movementHelper.tick();

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isCantFindPath())
            colonist.teleport(this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ());
    }

    protected Item getWorkingItem() {
        return Items.STICK;
    }

    @Override
    public void stop(Colonist colonist) {
        this.blockPos = null;
        this.workingTicks = 0;
        colonist.getMovementHelper().reset();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return blockPos != null && colonist.world.isDay();
    }

    private void setupTablePos(Colonist colonist) {
        final BlockPos blockPos = getBlockPos(colonist);
        if (blockPos == null) return;
        this.blockPos = blockPos;
    }

    protected abstract BlockPos getBlockPos(Colonist colonist);

}

