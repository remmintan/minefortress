package org.minefortress.ai;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.minefortress.BuildingManager;
import org.minefortress.entity.Colonist;

public class ColonistNavigation extends MobNavigation {

    private final Colonist colonist;

    public ColonistNavigation(Colonist colonist, World level) {
        super(colonist, level);
        this.colonist = colonist;
        this.setCanSwim(true);
        this.setCanEnterOpenDoors(true);
        this.setCanPathThroughDoors(true);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int p_26453_) {
        final NodeMaker nodeMaker = new NodeMaker();
        nodeMaker.setCanEnterOpenDoors(true);
        nodeMaker.setCanSwim(true);
        nodeMaker.setCanOpenDoors(true);
        this.nodeMaker = nodeMaker;
        return new ColonistPathFinder(nodeMaker, p_26453_);
    }

    @Override
    public Path findPathTo(BlockPos pos, int reachDistance) {
        return super.findPathTo(ImmutableSet.of(pos), 32, false, reachDistance);
    }

    public void tick() {
        if(currentPath == null || currentPath.isFinished()) {
            super.tick();
            return;
        }

        Vec3d nextEntityPos = currentPath.getNodePosition(entity);
        double currentY = entity.getBlockY();
        double neededY = nextEntityPos.y - 1; // we need to subtract one to prevent from building scaffold on one block heojt

        if(flatHasReached(nextEntityPos) && neededY > currentY && this.colonist.fallDistance <= 1 && !this.colonist.isWallAboveTheHead()) {
            this.colonist.getScaffoldsControl().needAction();
        } else if(flatHasReached(nextEntityPos) &&
                neededY + 1 > currentY && this.colonist.fallDistance <= 1 &&
                !this.colonist.isWallAboveTheHead() &&
                BuildingManager.doesNotHaveCollisions(this.colonist.world, new BlockPos(nextEntityPos).down())
        ) {
            this.colonist.getScaffoldsControl().needAction();
        }
        else
        {
            super.tick();
        }
    }

    public static double flatDistanceBetween(Vec3d pos1, Vec3d pos2) {
        return Math.sqrt(Math.pow(pos1.x - pos2.x, 2) + Math.pow(pos1.z - pos2.z, 2));
    }

    private boolean flatHasReached(Vec3d nextPos) {
        return flatDistanceBetween(colonist.getEyePos(), nextPos) < Colonist.WORK_REACH_DISTANCE;
    }
}
