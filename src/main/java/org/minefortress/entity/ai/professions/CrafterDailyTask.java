package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

public class CrafterDailyTask implements ProfessionDailyTask{

    private static final int MAX_WORK_TIME = 200;

    private BlockPos tablePos;
    private int workingTicks;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.world.isDay();
    }

    @Override
    public void start(Colonist colonist) {
        this.setupTablePos(colonist);
        colonist.getMovementHelper().set(this.tablePos);
    }

    @Override
    public void tick(Colonist colonist) {
        if(this.tablePos == null) return;
        final MovementHelper movementHelper = colonist.getMovementHelper();
        if(movementHelper.hasReachedWorkGoal()) {
            if(workingTicks % 10 == 0) {
                colonist.swingHand(colonist.world.random.nextFloat() < 0.5F? Hand.MAIN_HAND : Hand.OFF_HAND);
                colonist.putItemInHand(Items.STICK);
            }
            colonist.lookAt(tablePos);
            workingTicks++;
        }
        movementHelper.tick();

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isCantFindPath())
            colonist.teleport(this.tablePos.getX(), this.tablePos.getY(), this.tablePos.getZ());
    }

    @Override
    public void stop(Colonist colonist) {
        this.tablePos = null;
        this.workingTicks = 0;
        colonist.getMovementHelper().reset();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return tablePos != null && colonist.world.isDay() && workingTicks < MAX_WORK_TIME;
    }

    private void setupTablePos(Colonist colonist) {
        final Optional<FortressServerManager> fortressManagerOpt = colonist.getFortressServerManager();
        if(fortressManagerOpt.isEmpty()) return;
        final FortressServerManager fortressManager = fortressManagerOpt.get();
        Optional<BlockPos> tablePosOpt = fortressManager.getSpecialBlocksByType(Blocks.CRAFTING_TABLE, true)
                .stream()
                .findFirst();

        if(tablePosOpt.isEmpty()) {
            tablePosOpt = fortressManager.getSpecialBlocksByType(Blocks.CRAFTING_TABLE, false)
                    .stream()
                    .findFirst();
        }

        if(tablePosOpt.isEmpty()) return;
        this.tablePos = tablePosOpt.get();
    }

    @Override
    public boolean isWorkTimeout() {
        return workingTicks >= MAX_WORK_TIME;
    }

}
