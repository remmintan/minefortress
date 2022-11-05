package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.IFortressAwareEntity;
import org.minefortress.fortress.FortressServerManager;

public class FortressEscapeDangerGoal extends EscapeDangerGoal {

    private final IFortressAwareEntity colonist;

    public FortressEscapeDangerGoal(Colonist mob, double speed) {
        super(mob, speed);
        this.colonist = mob;
    }

    @Override
    public boolean canStart() {
        return super.canStart() && !isFighting();
    }

    @Override
    public void start() {
        super.start();
        colonist.getFortressServerManager().ifPresent(it -> {
            if(it.isCombatMode()) return;
            it.setCombatMode(true, true);
//            colonist.sendMessageToMasterPlayer("§a Village is under attack!  Defend it!§a");
//            it.getServerFightManager().addScaryMob(this.colonist.getAttacker());
        });
    }

    protected boolean isFighting() {
        return isFortressInCombatMode();
    }

    private boolean isFortressInCombatMode() {
        return colonist.getFortressServerManager().map(FortressServerManager::isCombatMode).orElse(false);
    }

}
