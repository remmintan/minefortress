package org.minefortress.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;

import java.util.EnumSet;

abstract class AbstractFortressGoal extends Goal {

    protected final Colonist colonist;

    protected AbstractFortressGoal(Colonist colonist) {
        this.colonist = colonist;
//        if(setControls)
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK,Goal.Control.JUMP));
        World level = this.colonist.world;
        if (!(level instanceof ServerWorld)) {
            throw new IllegalStateException("AI should run on the server entities!");
        }
    }

    protected boolean isFighting() {
        return isFortressInCombatMode() && colonist.getFightControl().isDefender();
    }

    protected boolean isHiding() {
        return isFortressInCombatMode() && !colonist.getFightControl().isDefender() && isVillageUnderAttack();
    }

    private boolean isFortressInCombatMode() {
        return colonist.getFortressServerManager().isCombatMode();
    }

    protected boolean isVillageUnderAttack() {
        return colonist.getFortressServerManager().isVillageUnderAttack();
    }

    protected boolean notInCombat() {
        return !isFighting() && !isHiding();
    }

    protected boolean isStarving() {
        return colonist.getCurrentFoodLevel() <= 0;
    }

    protected boolean isScared() {
        final var fightControl = colonist.getFightControl();
        final var canSeeMonster = fightControl.canSeeMonster();
        if(!canSeeMonster || fightControl.isWarrior() || isFighting()) return false;
        final var target = colonist.getTarget();

        return Math.sqrt(colonist.squaredDistanceTo(target)) <= 2;
    }

}
