package org.minefortress.entity.ai.professions;

import kotlin.jvm.functions.Function1;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ModLogger;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.minefortress.MineFortressMod;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.professions.fishing.FisherBlockFounderKt;
import org.minefortress.entity.ai.professions.fishing.FisherGoal;
import org.minefortress.entity.fisher.FortressFishingBobberEntity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FisherDailyTask implements ProfessionDailyTask {
    private long stopTime = 0L;
    private long workingTicks = 0L;

    private int catchCooldown = 0;
    private volatile FisherGoal goal;
    private Future<FisherGoal> goalFuture;
    private FortressFishingBobberEntity fishingBobberEntity;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getWorld().isDay() && colonist.getWorld().getTime() - this.stopTime > 800L;
    }

    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Catch fish");
        this.goal = null;
        this.goalFuture = MineFortressMod.getExecutor().submit(() -> this.setGoalAsync(colonist));
    }

    @Override
    public void tick(Colonist colonist) {
        if(catchCooldown > 0) catchCooldown--;

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
            if (this.fishingBobberEntity == null && catchCooldown <= 0) {
                colonist.swingHand(Hand.MAIN_HAND);
                this.fishingBobberEntity = new FortressFishingBobberEntity(
                        colonist,
                        colonist.getWorld(),
                        0,
                        0
                );
                this.fishingBobberEntity.setPosition(goal.getWaterPos().toCenterPos());
                colonist.getWorld().spawnEntity(fishingBobberEntity);
            }

            if(this.fishingBobberEntity!=null && this.fishingBobberEntity.hasHookedSomething()) {
                colonist.swingHand(Hand.MAIN_HAND);
                this.fishingBobberEntity.use(colonist.getStackInHand(Hand.MAIN_HAND));
                if(!this.fishingBobberEntity.isAlive()) {
                    this.fishingBobberEntity = null;
                    catchCooldown=10;
                }
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
        colonist.putItemInHand(null);
        colonist.resetControls();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return (goal != null || goalFuture != null) && colonist.getWorld().isDay() && workingTicks < 800L;
    }

    private FisherGoal setGoalAsync(Colonist pawn) {
        final var managersProvider = ServerModUtils.getManagersProvider(pawn);
        final var buildingsManager = managersProvider.getBuildingsManager();
        final var buildingOpt = buildingsManager.findNearest(pawn.getBlockPos(), ProfessionType.FISHERMAN);
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

            final var goalOpt2 = FisherBlockFounderKt.getFisherGoal(
                    pawn,
                    center,
                    it -> world.getBlockState(it).isOf(Blocks.WATER)
            );
            if(goalOpt2.isPresent()) {
                return goalOpt2.get();
            }
        }

        // look for water near campfire
        final var fortressManager = ServerModUtils.getFortressManager(pawn);
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
