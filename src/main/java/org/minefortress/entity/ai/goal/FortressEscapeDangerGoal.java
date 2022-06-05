package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;

public class FortressEscapeDangerGoal extends EscapeDangerGoal {

    private final Colonist colonist;

    public FortressEscapeDangerGoal(Colonist mob, double speed) {
        super(mob, speed);
        this.colonist = mob;
    }

    @Override
    public boolean canStart() {
        return super.canStart() && !isFighting() && !colonist.getFightControl().isWarrior();
    }

    @Override
    public void start() {
        super.start();
        colonist.getFortressServerManager().ifPresent(it -> {
            it.setCombatMode(true, true);
            colonist.doActionOnMasterPlayer(p -> ((ServerPlayerEntity)p).sendMessage(new LiteralText("§a Village is under attack!  Defend it!§a"), false));
            it.getServerFightManager().addScaryMob(this.colonist.getAttacker());
        });
    }

    protected boolean isFighting() {
        return isFortressInCombatMode() && colonist.getFightControl().isDefender();
    }

    private boolean isFortressInCombatMode() {
        return colonist.getFortressServerManager().map(FortressServerManager::isCombatMode).orElse(false);
    }

}
