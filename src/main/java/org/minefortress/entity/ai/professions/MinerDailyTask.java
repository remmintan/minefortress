package org.minefortress.entity.ai.professions;

import net.minecraft.registry.tag.FluidTags;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;

public class MinerDailyTask extends AbstractAutomationAreaTask{

    private IAutomationBlockInfo goal;

    @Override
    public void tick(Colonist colonist) {
        if (area == null) return;
        final var movementHelper = colonist.getMovementHelper();

        if(goal == null) {
            if(!iterator.hasNext()) return;
            goal = iterator.next();
        }

        if (goal != null && movementHelper.getGoal() == null) {
            movementHelper.goTo(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
        }

        if (movementHelper.hasReachedGoal() && colonist.getPlaceControl().isDone() && colonist.getDigControl().isDone())
            doActionWithTheGoal(colonist);


        if (movementHelper.getGoal() != null && !movementHelper.hasReachedGoal() && movementHelper.isStuck()) {
            final var workGoal = movementHelper.getGoal().up();
            colonist.teleport(workGoal.getX() + 0.5, workGoal.getY(), workGoal.getZ() + 0.5);
        }
    }

    private void doActionWithTheGoal(Colonist colonist) {
        final var blockState = colonist.getWorld().getBlockState(goal.pos());
        if(blockState.isAir() || blockState.getFluidState().isIn(FluidTags.WATER)) {
            colonist.getMovementHelper().reset();
            this.goal = null;
        } else {
            // TODO: check if this miner level can actually dig this block
            colonist.setGoal(new DigTaskBlockInfo(goal.pos()));
        }
    }

    @Override
    public void stop(Colonist colonist) {
        super.stop(colonist);
        this.goal = null;
    }

    @Override
    protected ProfessionType getProfessionType() {
        return ProfessionType.MINER;
    }

    @Override
    protected String getTaskDesc() {
        return "Mining";
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.getWorld().isDay() && (iterator.hasNext() || this.goal != null);
    }
}
