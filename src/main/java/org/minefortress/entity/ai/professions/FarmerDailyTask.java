package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.IAutomationArea;
import org.minefortress.fortress.automation.AutomationActionType;
import org.minefortress.fortress.automation.AutomationBlockInfo;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;
import org.spongepowered.include.com.google.common.collect.Sets;

import java.util.*;

public class FarmerDailyTask implements ProfessionDailyTask{

    private static final Set<Item> FARMER_SEEDS = Sets.newHashSet(
            Items.WHEAT_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.CARROT,
            Items.POTATO,
            Items.COCOA_BEANS
    );

    private IAutomationArea currentFarm;
    private Iterator<AutomationBlockInfo> farmIterator;
    private AutomationBlockInfo goal;
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
        initIterator(colonist);
        colonist.getBaritone().settings().allowParkour.set(false);
    }

    @Override
    public void tick(Colonist colonist) {
        if(this.currentFarm == null) return;

        final var movementHelper = colonist.getMovementHelper();
        if(this.goal == null) {
            do {
                if(!this.farmIterator.hasNext()) return;
                this.goal = this.farmIterator.next();
            } while(goalAlreadyInCorrectState(colonist));
            movementHelper.set(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
        }
        if(this.goal != null && movementHelper.getWorkGoal() == null) {
            movementHelper.set(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
        }

        if(movementHelper.hasReachedWorkGoal() && colonist.getPlaceControl().isDone() && colonist.getDigControl().isDone()) {
            switch (this.goal.info()) {
                case FARM_CROPS -> doFarmCrops(colonist);
                case FARM_WATER -> doSetWater(colonist);
            }
        }

        if(movementHelper.getWorkGoal() != null && !movementHelper.hasReachedWorkGoal() && movementHelper.isStuck()){
            final var workGoal = movementHelper.getWorkGoal().up();
            colonist.teleport(workGoal.getX(), workGoal.getY(), workGoal.getZ());
        }
    }

    private void doFarmCrops(Colonist colonist) {
        final var movementHelper = colonist.getMovementHelper();
        final var goalBlockState = colonist.world.getBlockState(this.goal.pos());
        if (goalBlockState.isOf(Blocks.DIRT) || goalBlockState.isOf(Blocks.GRASS_BLOCK)) {
            final var aboveBlock = goal.pos().up();
            final var aboveBlockState = colonist.world.getBlockState(aboveBlock);
            if(aboveBlockState.isIn(BlockTags.REPLACEABLE_PLANTS)) {
                colonist.setGoal(new DigTaskBlockInfo(aboveBlock));
            } else {
                colonist.putItemInHand(Items.WOODEN_HOE);
                colonist.swingHand(Hand.MAIN_HAND);
                colonist.world.setBlockState(goal.pos(), Blocks.FARMLAND.getDefaultState(), 3);
                colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal.pos());
            }
        } else if(goalBlockState.isOf(Blocks.FARMLAND)) {
            final var aboveBlock = goal.pos().up();
            final var aboveGoal = colonist.world.getBlockState(aboveBlock);

            if(aboveGoal.isIn(BlockTags.CROPS) && aboveGoal.getBlock() instanceof CropBlock cropBlock) {
                if(aboveGoal.get(cropBlock.getAgeProperty()) == cropBlock.getMaxAge()) {
                    final var digTaskBlockInfo = new DigTaskBlockInfo(aboveBlock);
                    colonist.setGoal(digTaskBlockInfo);
                } else {
                    this.goal = null;
                }
            } else if (aboveGoal.isAir()) {
                final var seedsOpt = getSeeds(colonist);
                if(seedsOpt.isPresent()) {
                    final var blockItem = (BlockItem) seedsOpt.get();
                    final var bsTaskBlockInfo = new BlockStateTaskBlockInfo(blockItem, aboveBlock, blockItem.getBlock().getDefaultState());
                    colonist.setGoal(bsTaskBlockInfo);
                    movementHelper.set(aboveBlock, Colonist.FAST_MOVEMENT_SPEED);
                } else {
                    this.goal = null;
                }
            } else {
                this.goal = null;
            }
        } else {
            this.goal = null;
        }
    }

    private void doSetWater(Colonist colonist) {
        final var goalBlockState = colonist.world.getBlockState(goal.pos());
        if(goalBlockState.isIn(BlockTags.SHOVEL_MINEABLE)) {
            colonist.setGoal(new DigTaskBlockInfo(goal.pos()));
        } else if(goalBlockState.isAir()) {
            colonist.putItemInHand(Items.WATER_BUCKET);
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.world.setBlockState(goal.pos(), Blocks.WATER.getDefaultState(), 3);
            colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal.pos());
        } else {
            this.goal = null;
        }
    }

    private boolean goalAlreadyInCorrectState(Colonist colonist) {
        final var goalBlockState = colonist.world.getBlockState(goal.pos());
        final var abovePos = goal.pos().up();
        final var aboveBlockState = colonist.world.getBlockState(abovePos);

        if(goal.info() == AutomationActionType.FARM_CROPS) {
            return goalBlockState.isOf(Blocks.FARMLAND) && aboveBlockState.isIn(BlockTags.CROPS) && aboveBlockState.get(CropBlock.AGE) < CropBlock.MAX_AGE;
        }

        if(goal.info() == AutomationActionType.FARM_WATER) {
            return goalBlockState.getFluidState().isIn(FluidTags.WATER);
        }

        return false;
    }

    @Override
    public void stop(Colonist colonist) {
        this.currentFarm = null;
        this.farmIterator = Collections.emptyIterator();
        this.stopTime = colonist.world.getTime();
        colonist.resetControls();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.world.isDay() && (farmIterator.hasNext() || this.goal != null);
    }

    private Optional<IAutomationArea> getFarm(Colonist colonist) {
        return colonist
            .getFortressServerManager()
            .flatMap(it -> it.getAutomationAreaByRequirementId("farmer"));
    }

    private boolean isEnoughTimeSinceLastTimePassed(Colonist colonist) {
        return colonist.world.getTime() - this.stopTime > 100;
    }

    private void initIterator(Colonist pawn) {
        if(this.currentFarm == null) {
            this.farmIterator = Collections.emptyIterator();
        } else {
            this.currentFarm.update();
            this.farmIterator = this.currentFarm.iterator(pawn.world);
        }
    }

    private Optional<BlockItem> getSeeds(Colonist colonist) {
        if(isCreative(colonist)) {
            return Optional.of((BlockItem) Items.WHEAT_SEEDS);
        }
        final var serverResourceManager = colonist.getFortressServerManager().orElseThrow().getServerResourceManager();
        final var itemOpt = serverResourceManager
                .getAllItems()
                .stream()
                .filter(it -> !it.isEmpty())
                .map(ItemStack::getItem)
                .filter(FARMER_SEEDS::contains)
                .min(Comparator.comparingInt(it -> {
                    if (it == Items.WHEAT_SEEDS) return 0;
                    if (it == Items.POTATO) return 1;
                    if (it == Items.CARROT) return 2;
                    return 3;
                }));

        itemOpt.ifPresent(serverResourceManager::removeItemIfExists);
        return itemOpt.map(it -> (BlockItem) it);
    }

    private static boolean isCreative(Colonist colonist) {
        return colonist.getFortressServerManager().map(FortressServerManager::isCreative).orElse(false);
    }
}
