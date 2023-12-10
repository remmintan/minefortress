package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ModLogger;
import org.minefortress.entity.Colonist;

public class FisherDailyTask implements ProfessionDailyTask {

    private static final int SEARCH_RADIUS = 50;

    private long stopTime = 0L;
    private long workingTicks = 0L;
    private BlockPos goal;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getWorld().isDay() && colonist.getWorld().getTime() - this.stopTime > 100L;
    }

    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Catch fish");
        this.setGoal(colonist);
        colonist.getMovementHelper().goTo(this.goal, Colonist.FAST_MOVEMENT_SPEED);
    }

    @Override
    public void tick(Colonist colonist) {
        if(goal == null) return;
        final var movementHelper = colonist.getMovementHelper();

        if(movementHelper.hasReachedWorkGoal()) {
            colonist.swingHand(colonist.getWorld().random.nextFloat() < 0.5F? Hand.MAIN_HAND : Hand.OFF_HAND);
            colonist.putItemInHand(Items.FISHING_ROD);
            colonist.lookAt(goal);
            workingTicks++;
//            if(colonist.getWorld().getBlockState(goal).isOf(Blocks.WATER)) {
//                colonist.addHunger(PASSIVE_EXHAUSTION);
//                colonist.addExperience(FORESTER_ITEMS);
//                colonist.getInventory().add(Items.COD);
//            }
        }

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isStuck())
            colonist.teleport(this.goal.getX(), this.goal.getY(), this.goal.getZ());
    }

    @Override
    public void stop(Colonist colonist) {
        this.stopTime = colonist.getWorld().getTime();
        this.goal = null;
        this.workingTicks = 0L;
        colonist.resetControls();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return goal != null && colonist.getWorld().isDay() && workingTicks < 200L;
    }

    private void setGoal(Colonist colonist) {
        if(this.goal != null){
            ModLogger.LOGGER.warn("Fisher goal is already set. Not setting new one.");
            return;
        }

        final var managersProviderOpt = colonist.getManagersProvider();
        if(managersProviderOpt.isEmpty()) return;
        final var managersProvider = managersProviderOpt.get();
        final var buildingsManager = managersProvider.getBuildingsManager();
        final var buildingOpt = buildingsManager.findNearest(colonist.getBlockPos(), "fisher");
        if(buildingOpt.isPresent()) {
            final var building = buildingOpt.get();
            final var center = building.getCenter();
            final var world = colonist.getWorld();
            final var closestWaterBlock = BlockPos.findClosest(center, SEARCH_RADIUS, SEARCH_RADIUS, it -> world.getBlockState(it).isOf(Blocks.WATER));
            closestWaterBlock.ifPresent(it -> this.goal = it);
        }

        // look for water near campfire
        if(goal == null) {
            final var fortressManagerOpt = colonist.getServerFortressManager();
            if(fortressManagerOpt.isEmpty()) return;
            final var fortressManager = fortressManagerOpt.get();
            final var fortressCenter = fortressManager.getFortressCenter();
            final var world = colonist.getWorld();
            final var closestWaterBlock = BlockPos.findClosest(fortressCenter, SEARCH_RADIUS, SEARCH_RADIUS, it -> world.getBlockState(it).isOf(Blocks.WATER));
            closestWaterBlock.ifPresent(it -> this.goal = it);
        }

        // if goal is still not set then send a message to the player
        if(goal == null) {
            colonist
                .getMasterPlayer()
                .ifPresent(player ->
                        player.sendMessage(Text.of("Fisherman can't find any source of water nearby"), false));
        }

    }
}
