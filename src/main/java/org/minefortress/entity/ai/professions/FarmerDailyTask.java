package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressBuilding;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;
import org.spongepowered.include.com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class FarmerDailyTask implements ProfessionDailyTask{

    private static final Set<Item> FARMER_SEEDS = Sets.newHashSet(
            Items.WHEAT_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.CARROT,
            Items.POTATO,
            Items.COCOA_BEANS
    );

    private FortressBuilding currentFarm;
    private Iterator<BlockPos> farmIterator;
    private BlockPos goal;
    private long stopTime = 0L;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.world.isDay() && isEnoughTimeSinceLastTimePassed(colonist);
    }

    @Override
    public void start(Colonist colonist) {
        colonist.resetControls();
        colonist.setCurrentTaskDesc("Farming");
        getFarm(colonist).ifPresent(f -> this.currentFarm = f);
        initIterator();
        colonist.getBaritone().settings().allowParkour.set(false);
    }

    @Override
    public void tick(Colonist colonist) {
        if(this.currentFarm == null) return;
        if(!this.farmIterator.hasNext()) return;
        final var movementHelper = colonist.getMovementHelper();
        if(this.goal == null) {
            findCorrectGoal(colonist);
            if(this.goal == null) return;
            movementHelper.set(goal.up(), Colonist.FAST_MOVEMENT_SPEED);
        }
        if(this.goal != null && movementHelper.getWorkGoal() == null) {
            movementHelper.set(goal.up(), Colonist.FAST_MOVEMENT_SPEED);
        }

        if(movementHelper.getWorkGoal() != null && movementHelper.hasReachedWorkGoal()) {
            final var goalBLockState = colonist.world.getBlockState(this.goal);
            if (goalBLockState.isOf(Blocks.DIRT) || goalBLockState.isOf(Blocks.GRASS_BLOCK)) {
                colonist.putItemInHand(Items.WOODEN_HOE);
                colonist.swingHand(Hand.MAIN_HAND);
                colonist.world.setBlockState(goal, Blocks.FARMLAND.getDefaultState(), 3);
                colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal);
            } else if(goalBLockState.isOf(Blocks.FARMLAND)) {
                if(colonist.getPlaceControl().isDone() && colonist.getDigControl().isDone()) {
                    final var aboveBlock = goal.up();
                    final var aboveGoal = colonist.world.getBlockState(aboveBlock);

                    if(aboveGoal.isIn(BlockTags.CROPS) && aboveGoal.getBlock() instanceof CropBlock cropBlock) {
                        if(aboveGoal.get(cropBlock.getAgeProperty()) == cropBlock.getMaxAge()) {
                            final var digTaskBlockInfo = new DigTaskBlockInfo(aboveBlock);
                            colonist.setGoal(digTaskBlockInfo);
                        } else {
                            this.goal = null;
                        }
                    } else if (aboveGoal.isAir()) {
                        if(isCreative(colonist)) {
                            final var wheatSeeds = (BlockItem) Items.WHEAT_SEEDS;
                            final var blockStateTaskBlockInfo = new BlockStateTaskBlockInfo(wheatSeeds, aboveBlock, wheatSeeds.getBlock().getDefaultState());
                            colonist.setGoal(blockStateTaskBlockInfo);
                            movementHelper.set(aboveBlock, Colonist.FAST_MOVEMENT_SPEED);
                        } else {
                            final var seedsOpt = getSeeds(colonist);
                            if(seedsOpt.isPresent()) {
                                final var blockItem = (BlockItem) seedsOpt.get();
                                final var bsTaskBlockInfo = new BlockStateTaskBlockInfo(blockItem, aboveBlock, blockItem.getBlock().getDefaultState());
                                colonist.setGoal(bsTaskBlockInfo);
                                movementHelper.set(aboveBlock, Colonist.FAST_MOVEMENT_SPEED);
                            } else {
                                this.goal = null;
                            }
                        }
                    } else {
                        this.goal = null;
                    }
                }
            } else {
                this.goal = null;
            }
        }

        if(movementHelper.getWorkGoal() != null && !movementHelper.hasReachedWorkGoal() && movementHelper.isStuck()){
            final var workGoal = movementHelper.getWorkGoal().up();
            colonist.teleport(workGoal.getX(), workGoal.getY(), workGoal.getZ());
        }

    }

    @Override
    public void stop(Colonist colonist) {
        this.currentFarm = null;
        this.farmIterator = Collections.emptyIterator();
        this.stopTime = colonist.world.getTime();
        colonist.getBaritone().settings().allowParkour.set(true);
        colonist.resetControls();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.world.isDay() && farmIterator.hasNext();
    }

    private Optional<FortressBuilding> getFarm(Colonist colonist) {
        return colonist
            .getFortressServerManager()
            .getRandomBuilding("farmer", colonist.world.random);
    }

    private boolean isEnoughTimeSinceLastTimePassed(Colonist colonist) {
        return colonist.world.getTime() - this.stopTime > 400;
    }

    private void initIterator() {
        if(this.currentFarm == null) {
            this.farmIterator = Collections.emptyIterator();
        } else {
            this.farmIterator = BlockPos.iterate(this.currentFarm.getStart(), this.currentFarm.getEnd()).iterator();
        }
    }

    private Optional<Item> getSeeds(Colonist colonist) {
        final var serverResourceManager = colonist.getFortressServerManager().getServerResourceManager();
        final var itemOpt = serverResourceManager
                .getAllItems()
                .stream()
                .filter(it -> !it.isEmpty())
                .map(ItemStack::getItem)
                .filter(FARMER_SEEDS::contains)
                .findFirst();

        itemOpt.ifPresent(serverResourceManager::removeItemIfExists);
        return itemOpt;
    }

    private void findCorrectGoal(Colonist colonist) {
        while (farmIterator.hasNext()) {
            final var possibleGoal = this.farmIterator.next().toImmutable();
            if(isCorrectGoal(colonist.world, possibleGoal)) {
                this.goal = possibleGoal;
                return;
            }
        }
    }

    private boolean isCorrectGoal(World world, BlockPos goal) {
        final var blockState = world.getBlockState(goal);
        final var goalCorrect = blockState.isOf(Blocks.FARMLAND) || blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.GRASS_BLOCK);
        final var aboveGoalState = world.getBlockState(goal.up());
        final var aboveGoalCorrect = aboveGoalState.isIn(BlockTags.CROPS) || aboveGoalState.isAir();
        return goalCorrect && aboveGoalCorrect;
    }

    private boolean isCreative(Colonist colonist) {
        return colonist.getFortressServerManager().isCreative();
    }
}
