package org.minefortress.entity.ai.goal;

import net.minecraft.block.BedBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

public class SleepOnTheBedGoal extends AbstractFortressGoal {

    private BlockPos bedPos;

    public SleepOnTheBedGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(!isNight() || colonist.getTaskControl().hasTask()) return false;
        getFreeBed().ifPresent(it -> bedPos = it);
        return bedPos != null;
    }

    @Override
    public void start() {
        colonist.setCurrentTaskDesc("Going to sleep");
        moveToBed();
    }

    @Override
    public void tick() {
        if(bedPos == null || colonist.isSleeping()) return;
        final var movementHelper = colonist.getMovementHelper();
        if(movementHelper.stillTryingToReachGoal()) return;
        if(hasReachedTheBed()) {
            if(!colonist.isSleeping()) {
                if(colonist.world.getBlockState(bedPos).isIn(BlockTags.BEDS)) {
                    colonist.sleep(bedPos);
                    colonist.putItemInHand(null);
                }
            }
        } else if(movementHelper.isStuck()) {
            colonist.getFortressServerManager().flatMap(FortressServerManager::getRandomPositionAroundCampfire)
                    .ifPresent(it -> {
                        final var pos = it.up();
                        colonist.resetControls();
                        colonist.teleport(pos.getX(), pos.getY(), pos.getZ());
                        moveToBed();
                    });
        }
    }

    @Override
    public boolean shouldContinue() {
        return isNight() && bedStillValid() && !colonist.getTaskControl().hasTask();
    }

    @Override
    public void stop() {
        bedPos = null;
        colonist.getMovementHelper().reset();
        if(colonist.isSleeping()) {
            colonist.wakeUp();
        }
    }

    @NotNull
    private Optional<BlockPos> getFreeBed() {
        return colonist.getFortressServerManager().flatMap(FortressServerManager::getFreeBed);
    }

    private boolean isNight() {
        return colonist.world.isNight();
    }
    private boolean bedStillValid() {
        if(bedPos == null) return false;
        final var blockState = colonist.world.getBlockState(bedPos);
        return blockState.isIn(BlockTags.BEDS) && (!blockState.get(BedBlock.OCCUPIED) || colonist.isSleeping());
    }

    private void moveToBed() {
        if(bedPos == null) return;
        colonist.getMovementHelper().set(bedPos, Colonist.SLOW_MOVEMENT_SPEED);
    }

    private boolean hasReachedTheBed() {
        if(bedPos == null) return false;
        return bedPos.isWithinDistance(colonist.getPos(), 2);
    }


}
