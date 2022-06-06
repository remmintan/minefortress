package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.tasks.BuildingManager;

import static org.minefortress.entity.colonist.ColonistHungerManager.PASSIVE_EXHAUSTION;
import static org.minefortress.professions.ProfessionManager.FORESTER_ITEMS;

public class ForesterDailyTask implements ProfessionDailyTask{

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
        colonist.getMovementHelper().set(this.blockPos);
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
                    colonist.getMovementHelper().set(this.blockPos);
                    this.interactionsCount = 0;
                }
            }
            colonist.lookAt(blockPos);
            workingTicks++;
        }
        movementHelper.tick();

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isCantFindPath())
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
            final var fortressServerManagerOpt = colonist.getFortressServerManager();
            if(fortressServerManagerOpt.isPresent()){
                final var fortressServerManager = fortressServerManagerOpt.get();
                final var resourceManager = fortressServerManager.getServerResourceManager();
                resourceManager.increaseItemAmount(item, 1);
            }
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
                        20,
                        10,
                        (BlockPos pos) -> BlockTags.LEAVES.contains(world.getBlockState(pos).getBlock())
                );
        if(closestLeavesOpt.isPresent()){
            this.blockPos = closestLeavesOpt.get();
            return;
        }

        final var closestHoeMineableOpt = BlockPos
                .findClosest(
                        randPointAround,
                        20,
                        10,
                        (BlockPos pos) -> BlockTags.HOE_MINEABLE.contains(world.getBlockState(pos).getBlock())
                );

        if(closestHoeMineableOpt.isPresent()){
            this.blockPos = closestHoeMineableOpt.get();
            return;
        }

        final var closestGrassBlockOpt = BlockPos
                .findClosest(
                        randPointAround,
                        20,
                        10,
                        (BlockPos pos) -> world.getBlockState(pos).getBlock() == Blocks.GRASS || world.getBlockState(pos).getBlock() == Blocks.TALL_GRASS
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
                        20,
                        10,
                        pos -> BuildingManager.canStayOnBlock(world, pos)
                )
                .ifPresentOrElse(pos -> this.blockPos = pos, () -> this.blockPos = null);
    }

}
