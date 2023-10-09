package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;
import org.minefortress.fortress.automation.AutomationBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import org.minefortress.fortress.automation.iterators.FarmAreaIterator;
import org.minefortress.fortress.resources.ItemInfo;
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
            Items.PUMPKIN_SEEDS,
            Items.MELON_SEEDS
    );

    private IAutomationArea currentFarm;
    private Iterator<AutomationBlockInfo> farmIterator;
    private AutomationBlockInfo goal;
    private long stopTime = 0L;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getWorld().isDay() && isEnoughTimeSinceLastTimePassed(colonist);
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
            movementHelper.goTo(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
        }
        if(this.goal != null && movementHelper.getWorkGoal() == null) {
            movementHelper.goTo(goal.pos().up(), Colonist.FAST_MOVEMENT_SPEED);
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
        final var goalBlockState = colonist.getWorld().getBlockState(this.goal.pos());
        final var aboveBlockPos = goal.pos().up();
        final var aboveBlockState = colonist.getWorld().getBlockState(aboveBlockPos);
        if (goalBlockState.isOf(Blocks.DIRT) || goalBlockState.isOf(Blocks.GRASS_BLOCK)) {
            if(FarmAreaIterator.blockCanBeRemovedToPlantCrops(aboveBlockState)) {
                colonist.setGoal(new DigTaskBlockInfo(aboveBlockPos));
            } else {
                colonist.putItemInHand(Items.WOODEN_HOE);
                colonist.swingHand(Hand.MAIN_HAND);
                colonist.getWorld().setBlockState(goal.pos(), Blocks.FARMLAND.getDefaultState(), 3);
                colonist.getWorld().emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal.pos());
            }
        } else if(goalBlockState.isOf(Blocks.FARMLAND)) {
            if(aboveBlockState.isIn(BlockTags.CROPS) && aboveBlockState.getBlock() instanceof CropBlock cropBlock) {
                if(cropBlock.getAge(aboveBlockState) == cropBlock.getMaxAge()) {
                    final var digTaskBlockInfo = new DigTaskBlockInfo(aboveBlockPos);
                    colonist.setGoal(digTaskBlockInfo);
                } else {
                    this.goal = null;
                }
            } else if (aboveBlockState.isAir()) {
                final var seedsOpt = getSeeds(colonist);
                if(seedsOpt.isPresent()) {
                    final var blockItem = (BlockItem) seedsOpt.get();
                    final var bsTaskBlockInfo = new BlockStateTaskBlockInfo(blockItem, aboveBlockPos, blockItem.getBlock().getDefaultState());
                    colonist.setGoal(bsTaskBlockInfo);
                    movementHelper.goTo(aboveBlockPos, Colonist.FAST_MOVEMENT_SPEED);
                } else {
                    this.goal = null;
                }
            } else if (FarmAreaIterator.blockCanBeRemovedToPlantCrops(aboveBlockState)) {
                colonist.setGoal(new DigTaskBlockInfo(aboveBlockPos));
            } else {
                this.goal = null;
            }
        } else {
            this.goal = null;
        }
    }

    private void doSetWater(Colonist colonist) {
        final var goalBlockState = colonist.getWorld().getBlockState(goal.pos());
        if(goalBlockState.isIn(BlockTags.SHOVEL_MINEABLE)) {
            colonist.setGoal(new DigTaskBlockInfo(goal.pos()));
        } else if(goalBlockState.isAir()) {
            colonist.putItemInHand(Items.WATER_BUCKET);
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.getWorld().setBlockState(goal.pos(), Blocks.WATER.getDefaultState(), 3);
            colonist.getWorld().emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal.pos());
        } else {
            this.goal = null;
        }
    }

    private boolean goalAlreadyInCorrectState(Colonist colonist) {
        final var goalBlockState = colonist.getWorld().getBlockState(goal.pos());
        final var abovePos = goal.pos().up();
        final var aboveBlockState = colonist.getWorld().getBlockState(abovePos);

        if(goal.info() == AutomationActionType.FARM_CROPS) {
            return goalBlockState.isOf(Blocks.FARMLAND)
                    && aboveBlockState.isIn(BlockTags.CROPS)
                    && aboveBlockState.getBlock() instanceof CropBlock crops
                    && crops.getAge(aboveBlockState) < crops.getMaxAge();
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
        this.stopTime = colonist.getWorld().getTime();
        colonist.resetControls();
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.getWorld().isDay() && (farmIterator.hasNext() || this.goal != null);
    }

    private Optional<IAutomationArea> getFarm(Colonist colonist) {
        return colonist
            .getFortressServerManager()
            .flatMap(it -> it.getAutomationAreaByRequirementId("farmer"));
    }

    private boolean isEnoughTimeSinceLastTimePassed(Colonist colonist) {
        return colonist.getWorld().getTime() - this.stopTime > 100;
    }

    private void initIterator(Colonist pawn) {
        if(this.currentFarm == null) {
            this.farmIterator = Collections.emptyIterator();
        } else {
            this.currentFarm.update();
            this.farmIterator = this.currentFarm.iterator(pawn.getWorld());
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

        itemOpt.ifPresent(
                it -> serverResourceManager
                    .removeItems(Collections.singletonList(new ItemInfo(it, 1)))
        );
        return itemOpt.map(it -> (BlockItem) it);
    }

    private static boolean isCreative(Colonist colonist) {
        return colonist.getFortressServerManager().map(FortressServerManager::isCreative).orElse(false);
    }
}
