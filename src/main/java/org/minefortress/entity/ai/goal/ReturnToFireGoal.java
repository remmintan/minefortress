package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.NodeMaker;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerWorld;

import java.util.EnumSet;
import java.util.Random;

public class ReturnToFireGoal extends Goal {

    private final Random random = new Random();
    private final Colonist colonist;

    public ReturnToFireGoal(Colonist colonist) {
        super();
        this.colonist = colonist;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if(!isNight()) return false;

        final BlockPos fortressCenter = colonist.getFortressCenter();
        return doesNotHaveAnyOtherTask() &&
                fortressCenter != null &&
                colonist.squaredDistanceTo(fortressCenter.getX(), fortressCenter.getY(), fortressCenter.getZ()) > Math.pow(getHomeOuterRadius(), 2);
    }

    private boolean isNight() {
        return colonist.world.isNight();
    }

    @Override
    public boolean canStop() {
        return true;
    }
    
    private int getHomeOuterRadius() {
        return Math.max(getColonistsCount(), 5) * 4 / 5;
    }

    @NotNull
    private Integer getColonistsCount() {
        return colonist.getFortressServerManager().map(FortressServerManager::getColonistsCount).orElse(5);
    }

    private int getHomeInnerRadius() {
        return Math.max(getColonistsCount(), 5) * 2 / 5;
    }

    @Override
    public void start() {
        super.start();
        final BlockPos fortressCenter = colonist.getFortressCenter().toImmutable();

        final int x = random.nextInt(getHomeOuterRadius() - getHomeInnerRadius()) + getHomeInnerRadius() * (random.nextBoolean()?1:-1);
        final int z = random.nextInt(getHomeOuterRadius() - getHomeInnerRadius()) + getHomeInnerRadius() * (random.nextBoolean()?1:-1);

        BlockPos goal = new BlockPos(fortressCenter.getX() + x, fortressCenter.getY(), fortressCenter.getZ() + z);

        final EntityNavigation navigation = colonist.getNavigation();
        final NodeMaker nodeMaker = (NodeMaker)navigation.getNodeMaker();

        nodeMaker.setWallClimbMode(true);
        final Path path = navigation.findPathTo(goal, 1);
        nodeMaker.setWallClimbMode(false);
        this.colonist.getNavigation().startMovingAlong(path, 1.0D);

        this.colonist.setCurrentTaskDesc("Staying near campfire");
    }

    @Override
    public boolean shouldContinue() {
        return isNight() && doesNotHaveAnyOtherTask() && !this.colonist.getNavigation().isIdle();
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
