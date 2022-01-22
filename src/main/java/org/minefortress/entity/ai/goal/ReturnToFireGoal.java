package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;

import java.util.EnumSet;
import java.util.Random;

public class ReturnToFireGoal extends Goal {

    private final Random random = new Random();
    private static final int HOME_RADIUS = 5;
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
        final BlockPos goal = BlockPos.iterateRandomly(random, 1, fortressCenter, HOME_RADIUS-1).iterator().next();
        this.colonist.getNavigation().startMovingTo(goal.getX(), goal.getY(), goal.getZ(), 1.0D);
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
