package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.NodeMaker;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;

public class ReturnToFireGoal extends Goal {

    private final Random random = new Random();
    private static final int HOME_RADIUS = 4;
    private final Colonist colonist;

    public ReturnToFireGoal(Colonist colonist) {
        super();
        this.colonist = colonist;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        final BlockPos fortressCenter = colonist.getFortressCenter();
        return fortressCenter != null &&
                colonist.squaredDistanceTo(fortressCenter.getX(), fortressCenter.getY(), fortressCenter.getZ()) > Math.pow(HOME_RADIUS, 2);
    }

    @Override
    public void start() {
        super.start();
        final BlockPos fortressCenter = colonist.getFortressCenter().toImmutable();
        final Iterator<BlockPos> iterator = BlockPos.iterateRandomly(random, 2, fortressCenter, HOME_RADIUS - 1).iterator();
        BlockPos goal = iterator.next();
        if(goal.equals(fortressCenter)) {
            goal = iterator.next();
        }

        final EntityNavigation navigation = colonist.getNavigation();
        final NodeMaker nodeMaker = (NodeMaker)navigation.getNodeMaker();

        nodeMaker.setWallClimbMode(true);
        final Path path = navigation.findPathTo(goal, 1);
        nodeMaker.setWallClimbMode(false);
        this.colonist.getNavigation().startMovingAlong(path, 1.0D);
    }

    @Override
    public boolean shouldContinue() {
        return !this.colonist.getNavigation().isIdle();
    }

    @Override
    public void stop() {
        super.stop();
        this.colonist.getNavigation().stop();
    }
}
