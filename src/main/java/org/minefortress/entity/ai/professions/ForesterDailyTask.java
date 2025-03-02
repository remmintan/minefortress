package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;

import static org.minefortress.entity.colonist.FortressHungerManager.PASSIVE_EXHAUSTION;
import static org.minefortress.professions.ProfessionManager.FORESTER_ITEMS;

public class ForesterDailyTask implements ProfessionDailyTask{

    private BlockPos goal;
    private int workingTicks;
    private int interactionsCount = 0;
    private long stopTime = 0;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getWorld().isDay() && colonist.getWorld().getTime() - this.stopTime > 400;
    }

    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Looking for food");
        this.setGoal(colonist);
        colonist.getMovementHelper().goTo(this.goal, Colonist.FAST_MOVEMENT_SPEED);
    }

    @Override
    public void tick(Colonist colonist) {
        if(this.goal == null) return;
        final MovementHelper movementHelper = colonist.getMovementHelper();
        if(movementHelper.hasReachedWorkGoal()) {
            if(workingTicks % 10 * colonist.getHungerMultiplier() == 0) {
                colonist.swingHand(colonist.getWorld().random.nextFloat() < 0.5F? Hand.MAIN_HAND : Hand.OFF_HAND);
                colonist.putItemInHand(Items.WOODEN_HOE);
                colonist.addHunger(PASSIVE_EXHAUSTION);
                this.interactionsCount++;
                this.gatherItemAndAddToInventory(colonist);
                if(this.interactionsCount > 2) {
                    this.setGoal(colonist);
                    colonist.getMovementHelper().goTo(this.goal, Colonist.FAST_MOVEMENT_SPEED);
                    this.interactionsCount = 0;
                }
            }
            colonist.lookAt(goal);
            workingTicks++;
        }

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isStuck())
            colonist.teleport(this.goal.getX(), this.goal.getY(), this.goal.getZ());
    }

    @Override
    public void stop(Colonist colonist) {
        this.workingTicks = 0;
        this.interactionsCount = 0;
        this.stopTime = colonist.getWorld().getTime();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return this.goal != null && this.workingTicks < 200;
    }

    private void gatherItemAndAddToInventory(Colonist colonist) {
        if(isSuccess(colonist)){
            final var item = getRandomForesterItem(colonist);
            // Using ServerModUtils.getManagersProvider directly - no try/catch
            ServerModUtils.getManagersProvider(colonist)
                    .getResourceManager()
                    .increaseItemAmount(item, 1);
        }
    }

    private Item getRandomForesterItem(Colonist colonist){
        final var random = colonist.getWorld().random;
        return FORESTER_ITEMS.get(random.nextInt(FORESTER_ITEMS.size()));
    }

    private boolean isSuccess(Colonist colonist){
        final var random = colonist.getWorld().random;
        return random.nextInt(100) < 18;
    }

    private void setGoal(Colonist colonist) {
        final var world = colonist.getWorld();

        final var fortressCenter = ServerModUtils.getFortressManager(colonist).getFortressCenter();

        final var horizontalRange = 10;
        final var randPointAroundCenter = BlockPos.iterateRandomly(world.random, 1, fortressCenter, horizontalRange)
                .iterator()
                .next();

        this.goal = BlockPos
                .findClosest(
                        randPointAroundCenter,
                        horizontalRange,
                        horizontalRange,
                        pos -> world.getBlockState(pos).isOf(Blocks.GRASS) || world.getBlockState(pos).isOf(Blocks.TALL_GRASS)
                ).or(
                        () -> BlockPos
                                .findClosest(
                                        randPointAroundCenter,
                                        horizontalRange,
                                        horizontalRange,
                                        pos -> world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)
                                )
                ).or(
                        () -> BlockPos
                                .findClosest(
                                        randPointAroundCenter,
                                        horizontalRange,
                                        horizontalRange,
                                        pos -> BuildingHelper.canStayOnBlock(world, pos)
                                )
                                .map(BlockPos::up)
                )
                .orElseGet(colonist::getBlockPos);
    }

}
