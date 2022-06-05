package org.minefortress.entity.ai.professions;

import org.minefortress.entity.Colonist;

public class WarriorDailyTask implements ProfessionDailyTask{


    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getFightControl().hasAttackTarget();
    }

    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Fighting");
    }

    @Override
    public void tick(Colonist colonist) {
        if(!colonist.getFightControl().hasAttackTarget()) return;
        colonist.getFightControl().checkAndPutCorrectItemInHand();
        colonist.getFightControl().attackTargetIfPossible();
    }

    @Override
    public void stop(Colonist colonist) {
        colonist.getNavigation().stop();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.getFightControl().hasAttackTarget();
    }

}
