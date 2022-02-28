package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.NodeMaker;
import org.minefortress.interfaces.FortressServerWorld;

import java.util.EnumSet;
import java.util.Random;

public class ReturnToFireGoal extends Goal {

    private final Random random = new Random();
    private static final int HOME_INNER_RADIUS = 2;
    private static final int HOME_OUTER_RADIUS = 4;
    private final Colonist colonist;

    public ReturnToFireGoal(Colonist colonist) {
        super();
        this.colonist = colonist;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        final BlockPos fortressCenter = colonist.getFortressCenter();
        return doesNotHaveAnyOtherTask() &&
                fortressCenter != null &&
                colonist.squaredDistanceTo(fortressCenter.getX(), fortressCenter.getY(), fortressCenter.getZ()) > Math.pow(HOME_OUTER_RADIUS, 2);
    }

    @Override
    public boolean canStop() {
        return true;
    }

    @Override
    public void start() {
        super.start();
        final BlockPos fortressCenter = colonist.getFortressCenter().toImmutable();

        final int x = random.nextInt(HOME_OUTER_RADIUS - HOME_INNER_RADIUS) + HOME_INNER_RADIUS * (random.nextBoolean()?1:-1);
        final int z = random.nextInt(HOME_OUTER_RADIUS - HOME_INNER_RADIUS) + HOME_INNER_RADIUS * (random.nextBoolean()?1:-1);

        BlockPos goal = new BlockPos(fortressCenter.getX() + x, fortressCenter.getY(), fortressCenter.getZ() + z);

        final EntityNavigation navigation = colonist.getNavigation();
        final NodeMaker nodeMaker = (NodeMaker)navigation.getNodeMaker();

        nodeMaker.setWallClimbMode(true);
        final Path path = navigation.findPathTo(goal, 1);
        nodeMaker.setWallClimbMode(false);
        this.colonist.getNavigation().startMovingAlong(path, 1.0D);
    }

    @Override
    public boolean shouldContinue() {
        return !this.colonist.getNavigation().isIdle() && doesNotHaveAnyOtherTask();
    }

    @Override
    public void stop() {
        super.stop();
        this.colonist.getNavigation().stop();
    }

    private boolean doesNotHaveAnyOtherTask() {
        final World world = colonist.getEntityWorld();
        final FortressServerWorld fortressWorld = (FortressServerWorld) world;
        return !fortressWorld.getTaskManager().hasTask();
    }
}
