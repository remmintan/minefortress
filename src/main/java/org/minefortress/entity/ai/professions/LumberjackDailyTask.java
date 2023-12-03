package org.minefortress.entity.ai.professions;

import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;
import org.minefortress.entity.Colonist;

public class LumberjackDailyTask extends AbstractAutomationAreaTask {

    private IAutomationBlockInfo goal;

    @Override
    protected String getAreaId() {
        return "lumberjack";
    }

    @Override
    protected String getTaskDesc() {
        return "Harvesting trees";
    }

    @Override
    public void tick(Colonist colonist) {
        if(area == null) return;
        final var movementHelper = colonist.getMovementHelper();

        if(goal == null) {
            if(!iterator.hasNext()) return;
            goal = iterator.next();
        }

        if (goal != null && movementHelper.getWorkGoal() == null) {
            movementHelper.goTo(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
        }

        if (movementHelper.hasReachedWorkGoal() && colonist.getPlaceControl().isDone() && colonist.getDigControl().isDone())
            this.goal = null;


        if(movementHelper.getWorkGoal() != null && !movementHelper.hasReachedWorkGoal() && movementHelper.isStuck()){
            final var workGoal = movementHelper.getWorkGoal().up().west();
            colonist.teleport(workGoal.getX() + 0.5, workGoal.getY(), workGoal.getZ() + 0.5);
        }
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.getWorld().isDay() && (iterator.hasNext() || this.goal != null);
    }

    @Override
    public void stop(Colonist colonist) {
        super.stop(colonist);
        this.goal = null;
    }
}
