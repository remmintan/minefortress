package org.minefortress.entity.ai.goal;

import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.controls.EatControl;
import org.minefortress.fortress.FortressServerManager;

import static org.minefortress.entity.colonist.FortressHungerManager.IDLE_EXHAUSTION;

public class WanderAroundTheFortressGoal extends AbstractFortressGoal {

    private BlockPos goal;

    public WanderAroundTheFortressGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(colonist.getEatControl().map(EatControl::isEating).orElse(false)) return false;
        if(!isDay() || colonist.getTaskControl().hasTask()) return false;
        return colonist.getFortressServerManager()
                .flatMap(FortressServerManager::randomSurfacePos)
                .isPresent();

    }

    @Override
    public void start() {
        colonist.getFortressServerManager().flatMap(FortressServerManager::randomSurfacePos)
                .ifPresent(it -> {
                    colonist.setCurrentTaskDesc("Wandering around");
                    colonist.putItemInHand(null);
                    goal = it;
                    colonist.getMovementHelper().set(goal, Colonist.SLOW_MOVEMENT_SPEED);
                    if(colonist.isSleeping()) {
                        colonist.wakeUp();
                    }
                });
    }

    @Override
    public void tick() {
        super.tick();
        colonist.addHunger(IDLE_EXHAUSTION);
        if(colonist.getMovementHelper().isStuck()) {
            if(goal != null) {
                colonist.teleport(goal.getX(), goal.getY(), goal.getZ());
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        return isDay() &&
                !colonist.getTaskControl().hasTask() &&
                !colonist.getMovementHelper().isStuck() &&
                colonist.getMovementHelper().stillTryingToReachGoal();
    }

    @Override
    public void stop() {
        goal = null;
        colonist.getMovementHelper().reset();
    }

    private boolean isDay() {
        return colonist.world.isDay();
    }
}
