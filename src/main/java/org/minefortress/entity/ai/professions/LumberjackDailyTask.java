package org.minefortress.entity.ai.professions;

import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;

public class LumberjackDailyTask extends AbstractAutomationAreaTask {

    private BlockPos goal;

    @Override
    protected String getAreaId() {
        return "tree harvesting";
    }

    @Override
    protected String getTaskDesc() {
        return "Harvesting trees";
    }

    @Override
    public void tick(Colonist colonist) {

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
