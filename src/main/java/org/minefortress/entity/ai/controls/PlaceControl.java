package org.minefortress.entity.ai.controls;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;

import java.util.Optional;

public class PlaceControl extends PositionedActionControl {

    private final Colonist colonist;

    private int placeCooldown = 0;
    private int failedInteractions = 0;

    public PlaceControl(Colonist colonist) {
        this.colonist = colonist;
    }

    @Override
    public void tick() {
        if(isDone()) return;
        if(!super.canReachTheGoal(colonist) || !colonist.getNavigation().isIdle()) return;

        if(placeCooldown>0) placeCooldown--;

        if(colonist.getBlockPos().equals(goal))
            colonist.getJumpControl().setActive();
        else
            placeBlock();
    }

    @Override
    public void reset() {
        super.reset();
        failedInteractions = 0;
    }

    protected void placeBlock() {
        colonist.lookAtGoal();
        colonist.putItemInHand(item);

        if (placeCooldown <= 0) {
            this.colonist.swingHand(Hand.MAIN_HAND);

            if(shouldBePlacedAsItem())
                placeAsItem();
            else
                placeAsBlock();
        }
    }

    private boolean shouldBePlacedAsItem() {
        if(item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET) return false;
        if(item instanceof TallBlockItem) return true;
        if(!(item instanceof final BlockItem blockItem)) return true;


        final Block block = blockItem.getBlock();
        final boolean solid = block.getDefaultState().getMaterial().isSolid();
        if(!solid) return true;
        return block instanceof BlockWithEntity;
    }

    private void placeAsItem() {
        ItemUsageContext context = getUseOnContext();
        final ActionResult interactionResult = item.useOnBlock(context);
        if(interactionResult == ActionResult.CONSUME || failedInteractions > 15) {
            this.reset();
            this.placeCooldown = 6;
        } else {
            failedInteractions++;
        }
    }

    private ItemPlacementContext getBlockPlaceContext() {
        return colonist.getBlockPlaceContext(item, goal);
    }

    private ItemUsageContext getUseOnContext() {
        return colonist.getUseOnContext(item, goal);
    }

    private void placeAsBlock() {
        BlockState stateForPlacement;
        if(isItemBucket(item)) {
            stateForPlacement = getBlockStateForBucketItem(item);
        } else {
            final BlockItem blockItem = (BlockItem) this.item;
            final Block block = blockItem.getBlock();
            final ItemPlacementContext blockPlaceContext = getBlockPlaceContext();
            stateForPlacement = Optional
                    .ofNullable(block.getPlacementState(blockPlaceContext))
                    .orElse(block.getDefaultState());
        }

        colonist.world.setBlockState(goal, stateForPlacement, 3);
        colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal);

        this.reset();
        this.placeCooldown = 6;
    }

    private BlockState getBlockStateForBucketItem(Item item) {
        if(item == Items.WATER_BUCKET) return Fluids.WATER.getDefaultState().getBlockState();
        if(item == Items.LAVA_BUCKET) return Fluids.LAVA.getDefaultState().getBlockState();
        return Blocks.AIR.getDefaultState();
    }

    private boolean isItemBucket(Item item) {
        return item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET || item instanceof BucketItem;
    }
}
