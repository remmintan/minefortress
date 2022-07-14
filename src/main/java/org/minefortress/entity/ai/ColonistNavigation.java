package org.minefortress.entity.ai;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;
import org.minefortress.utils.BuildingHelper;

public class ColonistNavigation extends MobNavigation {

    private final Colonist colonist;
    private boolean cantCreateScaffold = false;

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
        cantCreateScaffold = false;
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

        if(flatHasReached(nextEntityPos) && neededY > currentY && this.colonist.fallDistance <= 1) {
            needScaffold();
        } else if(flatHasReached(nextEntityPos) &&
                neededY + 1 > currentY && this.colonist.fallDistance <= 1 &&
                BuildingHelper.doesNotHaveCollisions(this.colonist.world, new BlockPos(nextEntityPos).down())
        ) {
            needScaffold();
        }
        else
        {
            super.tick();
        }
    }

    private void needScaffold() {
        final BlockState blockState = world.getBlockState(this.colonist.getBlockPos());
        final Block block = blockState.getBlock();
        if(block instanceof BedBlock || (colonist.isOnGround() && colonist.isWallAboveTheHead())) {
            cantCreateScaffold = true;
        } else {
            this.colonist.getScaffoldsControl().needAction();
        }
    }

    public boolean isCantCreateScaffold() {
        return cantCreateScaffold;
    }

    public static double flatDistanceBetween(Vec3d pos1, Vec3d pos2) {
        return Math.sqrt(Math.pow(pos1.x - pos2.x, 2) + Math.pow(pos1.z - pos2.z, 2));
    }

    private boolean flatHasReached(Vec3d nextPos) {
        return flatDistanceBetween(colonist.getEyePos(), nextPos) < Colonist.WORK_REACH_DISTANCE;
    }
}
