package org.minefortress.entity.ai.professions;

import org.minefortress.entity.Colonist;

public class LumberjackDailyTask implements ProfessionDailyTask {
    @Override
    public boolean canStart(Colonist colonist) {
        return false;
    }

    @Override
    public void start(Colonist colonist) {

    }

    @Override
    public void tick(Colonist colonist) {

    }

    @Override
    public void stop(Colonist colonist) {

    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return false;
    }
}
