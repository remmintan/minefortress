package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.NodeMaker;
import org.minefortress.fortress.FortressBedInfo;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

public class SleepOnTheBedGoal extends AbstractFortressGoal {

    private FortressBedInfo bedInfo;

    public SleepOnTheBedGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(!notInCombat() || !isNight() || colonist.getTaskControl().hasTask()) return false;

        final Optional<FortressBedInfo> freeBedOptional = getFreeBed();
        return freeBedOptional.isPresent();
    }

    @Override
    public void start() {
        final Optional<FortressBedInfo> freeBed = getFreeBed();
        if(freeBed.isPresent()) {
            bedInfo = freeBed.get();
            bedInfo.setOccupied(true);
            colonist.setCurrentTaskDesc("Going to sleep");
            moveToBed();
        }
    }

    @Override
    public void tick() {
        if(bedInfo == null) return;
        if(colonist.getNavigation().isIdle()) {
            if(hasReachedTheBed()) {
                if(!colonist.isSleeping()) {
                    final BlockPos bedPos = this.bedInfo.getPos();
                    if(BlockTags.BEDS.contains(colonist.world.getBlockState(bedPos).getBlock())) {
                        colonist.sleep(bedPos);
                        colonist.putItemInHand(null);
                    }
                }
            } else {
                moveToBed();
            }
        }
    }

    private void moveToBed() {
        if(bedInfo == null) return;
        final BlockPos pos = bedInfo.getPos();
        final EntityNavigation navigation = colonist.getNavigation();
        final NodeMaker colonistNodeMaker = (NodeMaker) navigation.getNodeMaker();
        colonistNodeMaker.setWallClimbMode(true);
        final Path pathTo = navigation.findPathTo(pos, 1);
        colonistNodeMaker.setWallClimbMode(false);

        navigation.startMovingAlong(pathTo, 1.5);
    }

    private boolean hasReachedTheBed() {
        if(bedInfo == null) return true;
        final BlockPos pos = bedInfo.getPos();
        return Math.sqrt(colonist.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())) < 2;
    }

    @Override
    public boolean shouldContinue() {
        return notInCombat() && isNight() && bedInfo != null && !colonist.getTaskControl().hasTask();
    }

    @Override
    public boolean canStop() {
        return true;
    }

    @Override
    public void stop() {
        colonist.getNavigation().stop();
        this.bedInfo.setOccupied(false);
        this.bedInfo = null;
        if(colonist.isSleeping()) {
            colonist.wakeUp();
        }
    }

    @NotNull
    private Optional<FortressBedInfo> getFreeBed() {
        return colonist
                .getFortressServerManager().flatMap(FortressServerManager::getFreeBed);
    }

    private boolean isNight() {
        return colonist.world.isNight();
    }

}
