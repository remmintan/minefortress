package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.NodeMaker;
import org.minefortress.fortress.FortressServerManager;

import java.util.EnumSet;
import java.util.Optional;

public class WanderAroundTheFortressGoal extends Goal {

    private final Colonist colonist;

    public WanderAroundTheFortressGoal(Colonist colonist) {
        this.colonist = colonist;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK,Goal.Control.JUMP));

    }

    @Override
    public boolean canStart() {
        if(!isDay() || colonist.getTaskControl().hasTask()) return false;
        final Optional<FortressServerManager> fortressManagerOpt = colonist.getFortressServerManager();
        if (fortressManagerOpt.isPresent()) {
            final FortressServerManager fortressManager = fortressManagerOpt.get();
            final Optional<BlockPos> blockPos = fortressManager.randomSurfacePos((ServerWorld) colonist.world);
            return blockPos.isPresent();
        }
        return false;
    }

    @Override
    public void start() {
        final Optional<FortressServerManager> fortressServerManagerOpt = colonist.getFortressServerManager();
        if (fortressServerManagerOpt.isPresent()) {
            final FortressServerManager fortressServerManager = fortressServerManagerOpt.get();
            final Optional<BlockPos> goalOpt = fortressServerManager.randomSurfacePos((ServerWorld) colonist.world);
            if(goalOpt.isPresent()) {
                colonist.setCurrentTaskDesc("Wandering around");
                final BlockPos goal = goalOpt.get();
                final EntityNavigation navigation = colonist.getNavigation();
                final NodeMaker nodeMaker = (NodeMaker)navigation.getNodeMaker();
                nodeMaker.setWallClimbMode(true);
                final Path path = navigation.findPathTo(goal, 3);
                nodeMaker.setWallClimbMode(false);
                navigation.startMovingAlong(path, 1.0D);
                if(colonist.isSleeping()) {
                    colonist.wakeUp();
                }
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        return isDay() && !this.colonist.getNavigation().isIdle() && !colonist.getTaskControl().hasTask();
    }

    @Override
    public boolean canStop() {
        return true;
    }

    @Override
    public void stop() {
        colonist.getNavigation().stop();
    }

    private boolean isDay() {
        return colonist.world.isDay();
    }
}
