package org.minefortress.entity.ai.goal;

import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

import static org.minefortress.entity.colonist.FortressHungerManager.IDLE_EXHAUSTION;

public class WanderAroundTheFortressGoal extends AbstractFortressGoal {

    private BlockPos goal;

    public WanderAroundTheFortressGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(!notInCombat() || !isDay() || colonist.getTaskControl().hasTask()) return false;
        final FortressServerManager fortressManager = colonist.getFortressServerManager();
        final Optional<BlockPos> blockPos = fortressManager.randomSurfacePos();
        return blockPos.isPresent();
    }

    @Override
    public void start() {
        final FortressServerManager fortressServerManager = colonist.getFortressServerManager();
        final Optional<BlockPos> goalOpt = fortressServerManager.randomSurfacePos();
        if(goalOpt.isPresent()) {
            colonist.setCurrentTaskDesc("Wandering around");
            colonist.putItemInHand(null);
            goal = goalOpt.get();
            colonist.getMovementHelper().set(goal, Colonist.SLOW_MOVEMENT_SPEED);
            if(colonist.isSleeping()) {
                colonist.wakeUp();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        colonist.addExhaustion(IDLE_EXHAUSTION);
        if(colonist.getMovementHelper().isStuck()) {
            if(goal != null) {
                colonist.teleport(goal.getX(), goal.getY(), goal.getZ());
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        return notInCombat() &&
                isDay() &&
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
