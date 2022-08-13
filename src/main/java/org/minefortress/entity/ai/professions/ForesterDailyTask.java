package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.utils.BuildingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.minefortress.entity.colonist.ColonistHungerManager.PASSIVE_EXHAUSTION;
import static org.minefortress.professions.ProfessionManager.FORESTER_ITEMS;

public class ForesterDailyTask implements ProfessionDailyTask{

    private static final Logger LOGGER = LoggerFactory.getLogger(ForesterDailyTask.class);

    private BlockPos blockPos;
    private int workingTicks;
    private int interactionsCount = 0;
    private long stopTime = 0;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.world.isDay() && colonist.world.getTime() - this.stopTime > 400;
    }

    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Looking for food");
        this.setGoal(colonist);
        colonist.getMovementHelper().set(this.blockPos, Colonist.FAST_MOVEMENT_SPEED);
    }

    @Override
    public void tick(Colonist colonist) {
        if(this.blockPos == null) return;
        final MovementHelper movementHelper = colonist.getMovementHelper();
        if(movementHelper.hasReachedWorkGoal()) {
            if(workingTicks % 10 * colonist.getHungerMultiplier() == 0) {
                colonist.swingHand(colonist.world.random.nextFloat() < 0.5F? Hand.MAIN_HAND : Hand.OFF_HAND);
                colonist.putItemInHand(Items.WOODEN_HOE);
                colonist.addExhaustion(PASSIVE_EXHAUSTION);
                this.interactionsCount++;
                this.gatherItemAndAddToInventory(colonist);
                if(this.interactionsCount > 2) {
                    this.setGoal(colonist);
                    colonist.getMovementHelper().set(this.blockPos, Colonist.FAST_MOVEMENT_SPEED);
                    this.interactionsCount = 0;
                }
            }
            colonist.lookAt(blockPos);
            workingTicks++;
        }

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isStuck())
            colonist.teleport(this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ());
    }

    @Override
    public void stop(Colonist colonist) {
        this.workingTicks = 0;
        this.interactionsCount = 0;
        this.stopTime = colonist.world.getTime();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return this.blockPos != null && this.workingTicks < 200;
    }

    private void gatherItemAndAddToInventory(Colonist colonist) {
        if(isSuccess(colonist)){
            final var item = getRandomForesterItem(colonist);
            colonist.getFortressServerManager()
                    .getServerResourceManager()
                    .increaseItemAmount(item, 1);
        }
    }

    private Item getRandomForesterItem(Colonist colonist){
        final var random = colonist.world.random;
        return FORESTER_ITEMS.get(random.nextInt(FORESTER_ITEMS.size()));
    }

    private boolean isSuccess(Colonist colonist){
        final var random = colonist.world.random;
        return random.nextInt(100) < 18;
    }

    private void setGoal(Colonist colonist){
        final var world = colonist.world;
        final var randPointAround = BlockPos.iterateRandomly(world.random, 1, colonist.getBlockPos(), 20).iterator().next();
        final var closestLeavesOpt = BlockPos
                .findClosest(
                        randPointAround,
                        21,
                        21,
                        (BlockPos pos) -> world.getBlockState(pos).isIn(BlockTags.LEAVES)
                );
        if(closestLeavesOpt.isPresent()){
            this.blockPos = closestLeavesOpt.get();
            return;
        }

        final var closestHoeMineableOpt = BlockPos
                .findClosest(
                        randPointAround,
                        21,
                        21,
                        (BlockPos pos) -> world.getBlockState(pos).isIn(BlockTags.HOE_MINEABLE)
                );

        if(closestHoeMineableOpt.isPresent()){
            this.blockPos = closestHoeMineableOpt.get();
            return;
        }

        final var closestGrassBlockOpt = BlockPos
                .findClosest(
                        randPointAround,
                        21,
                        21,
                        (BlockPos pos) -> (world.getBlockState(pos).isOf(Blocks.GRASS) || world.getBlockState(pos).isOf(Blocks.TALL_GRASS))
                );

        if(closestGrassBlockOpt.isPresent()){
            this.blockPos = closestGrassBlockOpt.get();
            return;
        }

        if(this.blockPos == null){
            this.blockPos = randPointAround;
        }

        BlockPos
                .findClosest(
                        this.blockPos,
                        21,
                        21,
                        pos -> BuildingHelper.canStayOnBlock(world, pos)
                )
                .ifPresentOrElse(pos -> this.blockPos = pos.up(), () -> this.blockPos = null);
        if(blockPos == null) {
            LOGGER.error("Could not find a valid block to work on");
        }
    }

}
