package org.minefortress.entity.ai.professions;

import net.minecraft.registry.tag.FluidTags;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public class MinerDailyTask implements ProfessionDailyTask{

    private long stopTime = 0L;

    private IAutomationArea currentMine;
    private Iterator<IAutomationBlockInfo> mineIterator;
    private IAutomationBlockInfo goal;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getWorld().isDay() && isEnoughTimeSinceLastTimePassed(colonist);
    }

    @Override
    public void start(Colonist colonist) {
        colonist.resetControls();
        colonist.setCurrentTaskDesc("Mining");
        getMine(colonist).ifPresent(m -> this.currentMine = m);
        initIterator(colonist);
    }

    @Override
    public void tick(Colonist colonist) {
        if (currentMine == null) return;
        final var movementHelper = colonist.getMovementHelper();

        if(goal == null) {
            if(!mineIterator.hasNext()) return;
            goal = mineIterator.next();
        }

        if (goal != null && movementHelper.getWorkGoal() == null) {
            movementHelper.goTo(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
        }

        if (movementHelper.hasReachedWorkGoal() && colonist.getPlaceControl().isDone() && colonist.getDigControl().isDone())
            doActionWithTheGoal(colonist);


        if(movementHelper.getWorkGoal() != null && !movementHelper.hasReachedWorkGoal() && movementHelper.isStuck()){
            final var workGoal = movementHelper.getWorkGoal().up();
            colonist.teleport(workGoal.getX() + 0.5, workGoal.getY(), workGoal.getZ() + 0.5);
        }
    }

    private void doActionWithTheGoal(Colonist colonist) {
        final var blockState = colonist.getWorld().getBlockState(goal.pos());
        if(blockState.isAir() || blockState.getFluidState().isIn(FluidTags.WATER)) {
            colonist.getMovementHelper().reset();
            this.goal = null;
        } else {
            colonist.setGoal(new DigTaskBlockInfo(goal.pos()));
        }
    }

    @Override
    public void stop(Colonist colonist) {
        this.currentMine = null;
        this.mineIterator = Collections.emptyIterator();
        this.stopTime = colonist.getWorld().getTime();
        this.goal = null;
        colonist.resetControls();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.getWorld().isDay() && (mineIterator.hasNext() || this.goal != null);
    }

    private void initIterator(Colonist colonist) {
        if(this.currentMine == null) {
            this.mineIterator = Collections.emptyIterator();
        } else {
            this.currentMine.update();
            this.mineIterator = this.currentMine.iterator(colonist.getWorld());
        }
    }

    private Optional<IAutomationArea> getMine(Colonist colonist) {
        return colonist.getServerFortressManager()
                .flatMap(it -> it.getAutomationAreaByRequirementId("miner", colonist.getMasterPlayer().orElse(null)));
    }

    private boolean isEnoughTimeSinceLastTimePassed(Colonist colonist) {
        return colonist.getWorld().getTime() - 100L >= this.stopTime;
    }
}
