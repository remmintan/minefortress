package org.minefortress.entity.ai.professions;

import org.minefortress.entity.Colonist;
import org.minefortress.fortress.IAutomationArea;
import org.minefortress.fortress.automation.AutomationBlockInfo;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public class MinerDailyTask implements ProfessionDailyTask{

    private long stopTime = 0L;

    private IAutomationArea currentMine;
    private Iterator<AutomationBlockInfo> mineIterator;
    private AutomationBlockInfo goal;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.world.isDay() && isEnoughTimeSinceLastTimePassed(colonist);
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
            movementHelper.set(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
        }

        if (movementHelper.hasReachedWorkGoal() && colonist.getPlaceControl().isDone() && colonist.getDigControl().isDone())
            doActionWithTheGoal(colonist);


        if(movementHelper.getWorkGoal() != null && !movementHelper.hasReachedWorkGoal() && movementHelper.isStuck()){
            final var workGoal = movementHelper.getWorkGoal().up();
            colonist.teleport(workGoal.getX(), workGoal.getY(), workGoal.getZ());
        }
    }

    private void doActionWithTheGoal(Colonist colonist) {
        if(colonist.world.getBlockState(goal.pos()).isAir()) {
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
        this.stopTime = colonist.world.getTime();
        this.goal = null;
        colonist.resetControls();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.world.isDay() && (mineIterator.hasNext() || this.goal != null);
    }

    private void initIterator(Colonist colonist) {
        if(this.currentMine == null) {
            this.mineIterator = Collections.emptyIterator();
        } else {
            this.currentMine.update();
            this.mineIterator = this.currentMine.iterator(colonist.world);
        }
    }

    private Optional<IAutomationArea> getMine(Colonist colonist) {
        return colonist.getFortressServerManager()
                .flatMap(it -> it.getAutomationAreaByRequirementId("miner"));
    }

    private boolean isEnoughTimeSinceLastTimePassed(Colonist colonist) {
        return colonist.world.getTime() - 100L >= this.stopTime;
    }
}
