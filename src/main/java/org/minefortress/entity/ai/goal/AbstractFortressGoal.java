package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.professions.ProfessionManager;

import java.util.EnumSet;

abstract class AbstractFortressGoal extends Goal {

    protected final Colonist colonist;

    protected AbstractFortressGoal(Colonist colonist) {
        this.colonist = colonist;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK,Goal.Control.JUMP));
        World level = this.colonist.world;
        if (!(level instanceof ServerWorld)) {
            throw new IllegalStateException("AI should run on the server entities!");
        }
    }

    protected boolean isFighting() {
        return isFortressInCombatMode() && isDefender();
    }

    protected boolean isHiding() {
        return isFortressInCombatMode() && !isDefender() && isVillageUnderAttack();
    }

    private boolean isDefender() {
        return ProfessionManager.DEFENDER_PROFESSIONS.contains(colonist.getProfessionId());
    }

    private boolean isFortressInCombatMode() {
        return colonist.getFortressServerManager().map(FortressServerManager::isCombatMode).orElse(false);
    }

    private boolean isVillageUnderAttack() {
        return colonist.getFortressServerManager().map(FortressServerManager::isVillageUnderAttack).orElse(false);
    }

    protected boolean notInCombat() {
        return !isFighting() && !isHiding();
    }

}
