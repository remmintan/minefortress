package org.minefortress.entity.ai.goal;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.minefortress.entity.Colonist;

import static org.minefortress.entity.colonist.FortressHungerManager.IDLE_EXHAUSTION;

public class WanderAroundTheFortressGoal extends AbstractFortressGoal {

    private BlockPos goal;

    public WanderAroundTheFortressGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(colonist.getEatControl().map(IEatControl::isEating).orElse(false)) return false;
        if(!isDay() || colonist.getTaskControl().hasTask()) return false;

        // Using ServerModUtils.getFortressManager instead of colonist.getServerFortressManager()
        final var posOptional = ServerModUtils.getFortressManager(colonist)
                .flatMap(IServerFortressManager::getRandomPositionAroundCampfire);

        if(posOptional.isPresent()) {
            goal = posOptional.get();
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        colonist.setCurrentTaskDesc("Wandering around");
        colonist.putItemInHand(null);
        colonist.getMovementHelper().goTo(goal, Colonist.SLOW_MOVEMENT_SPEED);
        if(colonist.isSleeping()) {
            colonist.wakeUp();
        }
    }

    @Override
    public void tick() {
        super.tick();
        colonist.addHunger(IDLE_EXHAUSTION);
        if(colonist.getMovementHelper().isStuck()) {
            if(goal != null) {
                colonist.resetControls();
                colonist.teleport(goal.getX(), goal.getY(), goal.getZ());
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        return isDay() &&
                !colonist.getTaskControl().hasTask() &&
                (colonist.getMovementHelper().stillTryingToReachGoal() || colonist.getMovementHelper().isStuck());
    }

    @Override
    public void stop() {
        goal = null;
        colonist.getMovementHelper().reset();
    }

    private boolean isDay() {
        return colonist.getWorld().isDay();
    }
}
