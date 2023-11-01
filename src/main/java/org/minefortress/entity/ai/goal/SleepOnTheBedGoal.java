package org.minefortress.entity.ai.goal;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import java.util.Optional;

public class SleepOnTheBedGoal extends AbstractFortressGoal {

    private BlockPos bedPos;

    public SleepOnTheBedGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(!isNight() || colonist.getTaskControl().hasTask()) return false;
        final var target = colonist.getTarget();
        if(target != null && target.isAlive()) return false;
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
                if(colonist.getWorld().getBlockState(bedPos).isIn(BlockTags.BEDS)) {
                    colonist.sleep(bedPos);
                    colonist.putItemInHand(null);
                }
            }
        } else if(movementHelper.isStuck()) {
            colonist.getServerFortressManager()
                    .flatMap(IServerFortressManager::getRandomPositionAroundCampfire)
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
        return colonist
                .getManagersProvider()
                .map(IServerManagersProvider::getBuildingsManager)
                .flatMap(IServerBuildingsManager::getFreeBed);
    }

    private boolean isNight() {
        return colonist.getWorld().isNight();
    }
    private boolean bedStillValid() {
        if(bedPos == null) return false;
        final var blockState = colonist.getWorld().getBlockState(bedPos);
        return blockState.isIn(BlockTags.BEDS) && (!blockState.get(BedBlock.OCCUPIED) || colonist.isSleeping());
    }

    private void moveToBed() {
        if(bedPos == null) return;
        colonist.getMovementHelper().goTo(bedPos, Colonist.SLOW_MOVEMENT_SPEED);
    }

    private boolean hasReachedTheBed() {
        if(bedPos == null) return false;
        return bedPos.isWithinDistance(colonist.getPos(), Colonist.WORK_REACH_DISTANCE);
    }


}
