package org.minefortress.entity.ai.professions;

import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;

public interface ProfessionDailyTask {

    boolean canStart(Colonist colonist);
    void start( Colonist colonist);
    void tick(Colonist colonist);
    void stop(Colonist colonist);
    boolean shouldContinue(Colonist colonist);
    boolean isWorkTimeout();

}
