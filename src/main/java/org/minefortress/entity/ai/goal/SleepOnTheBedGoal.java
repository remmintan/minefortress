package org.minefortress.entity.ai.goal;

import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.fortress.FortressBedInfo;

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
        final var movementHelper = colonist.getMovementHelper();
        if(movementHelper.stillTryingToReachGoal()) return;
        if(hasReachedTheBed()) {
            if(!colonist.isSleeping()) {
                final BlockPos bedPos = this.bedInfo.getPos();
                if(colonist.world.getBlockState(bedPos).isIn(BlockTags.BEDS)) {
                    colonist.sleep(bedPos);
                    colonist.putItemInHand(null);
                }
            }
        } else if(movementHelper.isStuck()) {
            moveToBed();
        }
    }

    private void moveToBed() {
        if(bedInfo == null) return;
        colonist.getMovementHelper().set(bedInfo.getPos(), Colonist.SLOW_MOVEMENT_SPEED);
    }

    private boolean hasReachedTheBed() {
        if(bedInfo == null) return true;
        final BlockPos pos = bedInfo.getPos();
        return Math.sqrt(colonist.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())) < 2;
    }

    @Override
    public boolean shouldContinue() {
        return notInCombat() && isNight() && bedInfo != null && !colonist.getTaskControl().hasTask() && !colonist.getMovementHelper().isStuck();
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
        return colonist.getFortressServerManager().getFreeBed();
    }

    private boolean isNight() {
        return colonist.world.isNight();
    }

}
