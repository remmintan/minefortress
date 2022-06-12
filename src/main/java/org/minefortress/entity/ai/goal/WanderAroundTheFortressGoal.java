package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.NodeMaker;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

import static org.minefortress.entity.colonist.ColonistHungerManager.IDLE_EXHAUSTION;

public class WanderAroundTheFortressGoal extends AbstractFortressGoal {


    public WanderAroundTheFortressGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(!notInCombat() || !isDay() || colonist.getTaskControl().hasTask()) return false;
        final FortressServerManager fortressManager = colonist.getFortressServerManager();
        final Optional<BlockPos> blockPos = fortressManager.randomSurfacePos((ServerWorld) colonist.world);
        return blockPos.isPresent();
    }

    @Override
    public void start() {
        final FortressServerManager fortressServerManager = colonist.getFortressServerManager();
        final Optional<BlockPos> goalOpt = fortressServerManager.randomSurfacePos((ServerWorld) colonist.world);
        if(goalOpt.isPresent()) {
            colonist.setCurrentTaskDesc("Wandering around");
            colonist.putItemInHand(null);
            final BlockPos goal = goalOpt.get();
            final EntityNavigation navigation = colonist.getNavigation();
            final NodeMaker nodeMaker = (NodeMaker)navigation.getNodeMaker();
            nodeMaker.setWallClimbMode(true);
            final Path path = navigation.findPathTo(goal, 1);
            nodeMaker.setWallClimbMode(false);
            navigation.startMovingAlong(path, 1.0D);
            if(colonist.isSleeping()) {
                colonist.wakeUp();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        colonist.addExhaustion(IDLE_EXHAUSTION);
    }

    @Override
    public boolean shouldContinue() {
        return notInCombat() && isDay() && !this.colonist.getNavigation().isIdle() && !colonist.getTaskControl().hasTask();
    }

    @Override
    public void stop() {
        colonist.getNavigation().stop();
    }

    private boolean isDay() {
        return colonist.world.isDay();
    }
}
