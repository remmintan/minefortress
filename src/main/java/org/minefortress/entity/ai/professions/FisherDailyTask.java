package org.minefortress.entity.ai.professions;

import kotlin.jvm.functions.Function1;
import net.minecraft.block.Blocks;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ModLogger;
import org.minefortress.MineFortressMod;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.professions.fishing.FakePlayerForFishing;
import org.minefortress.entity.ai.professions.fishing.FisherBlockFounderKt;
import org.minefortress.entity.ai.professions.fishing.FisherGoal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FisherDailyTask implements ProfessionDailyTask {
    private long stopTime = 0L;
    private long workingTicks = 0L;
    private volatile FisherGoal goal;
    private Future<FisherGoal> goalFuture;
    private FishingBobberEntity fishingBobberEntity;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getWorld().isDay() && colonist.getWorld().getTime() - this.stopTime > 200L;
    }

    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Catch fish");
        this.goal = null;
        this.goalFuture = MineFortressMod.getExecutor().submit(() -> this.setGoalAsync(colonist));
    }

    @Override
    public void tick(Colonist colonist) {
        if(goalFuture != null) {
            if(goalFuture.isDone()) {
                setTheGoal();
            } else {
                return;
            }
        }
        if(goal == null) return;

        final var movementHelper = colonist.getMovementHelper();
        final var earthPos = goal.getEarthPos();
        if(!earthPos.equals(movementHelper.getWorkGoal()))
            movementHelper.goTo(earthPos, Colonist.FAST_MOVEMENT_SPEED);

        if(movementHelper.hasReachedWorkGoal()) {
            colonist.putItemInHand(Items.FISHING_ROD);
            colonist.lookAt(goal.getWaterPos());
            workingTicks++;
            if (this.fishingBobberEntity == null) {
                final var player = FakePlayerForFishing.Companion.getFakePlayerForFinish(colonist);
                this.fishingBobberEntity = new FishingBobberEntity(player, colonist.getWorld(), 0, 0);
                colonist.getWorld().spawnEntity(fishingBobberEntity);
            }

//            if(colonist.getWorld().getBlockState(goal).isOf(Blocks.WATER)) {
//                colonist.addHunger(PASSIVE_EXHAUSTION);
//                colonist.addExperience(FORESTER_ITEMS);
//                colonist.getInventory().add(Items.COD);
//            }
        }

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isStuck())
            colonist.teleport(earthPos.getX(), earthPos.getY(), earthPos.getZ());


    }

    private void setTheGoal() {
        try {
            this.goal = goalFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            goalFuture = null;
        }
    }

    @Override
    public void stop(Colonist colonist) {
        this.stopTime = colonist.getWorld().getTime();
        if(goalFuture!=null && !goalFuture.isDone()) {
            goalFuture.cancel(true);
        }
        this.goalFuture = null;
        this.workingTicks = 0L;
        this.fishingBobberEntity=null;
        colonist.resetControls();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return (goal != null || goalFuture != null) && colonist.getWorld().isDay() && workingTicks < 200L;
    }

    private FisherGoal setGoalAsync(Colonist pawn) {
        final var managersProviderOpt = pawn.getManagersProvider();
        if(managersProviderOpt.isEmpty()) return null;
        final var managersProvider = managersProviderOpt.get();
        final var buildingsManager = managersProvider.getBuildingsManager();
        final var buildingOpt = buildingsManager.findNearest(pawn.getBlockPos(), "fisher");
        final var world = pawn.getWorld();
        if(buildingOpt.isPresent()) {
            final var building = buildingOpt.get();
            final var center = building.getCenter();
            final Function1<BlockPos, Boolean> predicate = it -> world.getBlockState(it).isOf(Blocks.WATER) &&
                    !buildingsManager.isPartOfAnyBuilding(it);
            final var goalOpt = FisherBlockFounderKt.getFisherGoal(pawn, center, predicate);
            if(goalOpt.isPresent()) {
                return goalOpt.get();
            }
        }

        // look for water near campfire
        final var fortressManagerOpt = pawn.getServerFortressManager();
        if(fortressManagerOpt.isEmpty()) return null;
        final var fortressManager = fortressManagerOpt.get();
        final var fortressCenter = fortressManager.getFortressCenter();
        final var goalOpt = FisherBlockFounderKt
                .getFisherGoal(pawn, fortressCenter, it -> world.getBlockState(it).isOf(Blocks.WATER));
        if(goalOpt.isPresent()) {
            return goalOpt.get();
        }


        // if goal is still not set then send a message to the player
        final var pawnName = pawn.getName().getString();
        ModLogger.LOGGER.info("Fisherman %s can't find any source of water nearby".formatted(pawnName));
        return null;
    }
}
