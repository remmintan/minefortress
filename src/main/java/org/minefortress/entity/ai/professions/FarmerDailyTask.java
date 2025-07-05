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
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.automation.iterators.FarmAreaIterator;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;
import org.spongepowered.include.com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class FarmerDailyTask extends AbstractAutomationAreaTask {

    private static final Set<Item> FARMER_SEEDS = Sets.newHashSet(
            Items.WHEAT_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.CARROT,
            Items.POTATO
    );

    private IAutomationBlockInfo goal;

    @Override
    public void start(Colonist colonist) {
        super.start(colonist);
        colonist.getBaritone().settings().allowParkour.set(false);
    }

    @Override
    public void tick(Colonist colonist) {
        if(this.area == null) return;

        final var movementHelper = colonist.getMovementHelper();
        if(this.goal == null) {
            do {
                if(!this.iterator.hasNext()) return;
                this.goal = this.iterator.next();
            } while(goalAlreadyInCorrectState(colonist));
            movementHelper.goTo(goal.pos().up());
        }
        if (this.goal != null && movementHelper.getGoal() == null) {
            movementHelper.goTo(goal.pos().up());
        }

        if (movementHelper.hasReachedGoal() && colonist.getPlaceControl().isDone() && colonist.getDigControl().isDone()) {
            switch (this.goal.info()) {
                case FARM_CROPS -> doFarmCrops(colonist);
                case FARM_WATER -> doSetWater(colonist);
            }
        }

        if (movementHelper.getGoal() != null && !movementHelper.hasReachedGoal() && movementHelper.isStuck()) {
            final var workGoal = movementHelper.getGoal().up();
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
                    final var blockItem = seedsOpt.get();
                    final var bsTaskBlockInfo = new BlockStateTaskBlockInfo(blockItem, aboveBlockPos, blockItem.getBlock().getDefaultState());
                    colonist.setGoal(bsTaskBlockInfo);
                    movementHelper.goTo(aboveBlockPos);
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
    public boolean shouldContinue(Colonist colonist) {
        return colonist.getWorld().isDay() && (iterator.hasNext() || this.goal != null);
    }

    @Override
    protected ProfessionType getProfessionType() {
        return ProfessionType.FARMER;
    }

    @Override
    protected String getTaskDesc() {
        return "Farming";
    }

    private Optional<BlockItem> getSeeds(Colonist colonist) {
        if (ServerExtensionsKt.isCreativeFortress(colonist.getServer())) {
            return Optional.of((BlockItem) Items.WHEAT_SEEDS);
        }

        // Use ServerModUtils to get resource manager directly
        return ServerModUtils.getManagersProvider(colonist)
                .map(IServerManagersProvider::getResourceManager)
                .flatMap(srm -> srm
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
                        }))
                        .map(it -> {
                            srm.removeItems(Collections.singletonList(new ItemInfo(it, 1)));
                            return (BlockItem) it;
                        })
                );
    }
}
