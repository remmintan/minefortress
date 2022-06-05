package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.professions.ProfessionManager;

public class FortressEscapeDangerGoal extends EscapeDangerGoal {

    private final Colonist colonist;

    public FortressEscapeDangerGoal(Colonist mob, double speed) {
        super(mob, speed);
        this.colonist = mob;
    }

    @Override
    public boolean canStart() {
        return super.canStart() && !isFighting() && !isWarrior();
    }

    protected boolean isFighting() {
        return isFortressInCombatMode() && isDefender();
    }

    private boolean isDefender() {
        return ProfessionManager.DEFENDER_PROFESSIONS.contains(colonist.getProfessionId());
    }

    private boolean isWarrior() {
        return ProfessionManager.WARRIOR_PROFESSIONS.contains(colonist.getProfessionId());
    }

    private boolean isFortressInCombatMode() {
        return colonist.getFortressServerManager().map(FortressServerManager::isCombatMode).orElse(false);
    }

}
