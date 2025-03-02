package org.minefortress.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.FortressBlockPlaceContext;
import org.minefortress.entity.ai.FortressUseOnContext;

import java.util.Optional;

public class BlockInfoUtils {

    public static boolean shouldBePlacedAsItem(Item item) {
        if(item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET) return false;
        if(item instanceof TallBlockItem) return true;
        if(item instanceof BedItem) return true;
        if(!(item instanceof final BlockItem blockItem)) return true;


        final Block block = blockItem.getBlock();
        final boolean solid = block.getDefaultState().isSolid();
        if(!solid) return true;
        return block instanceof BlockWithEntity;
    }

    public static BlockState getBlockStateForPlacement(Item item, HitResult hitResult, Direction direction, BlockPos pos, Colonist colonist) {
        BlockState stateForPlacement;
        if(isItemBucket(item)) {
            stateForPlacement = getBlockStateForBucketItem(item);
        } else {
            final BlockItem blockItem = (BlockItem) item;
            final Block block = blockItem.getBlock();
            final ItemPlacementContext blockPlaceContext = getBlockPlaceContext(hitResult, direction, item, pos, colonist);
            stateForPlacement = Optional
                    .ofNullable(block.getPlacementState(blockPlaceContext))
                    .orElse(block.getDefaultState());
        }

        return stateForPlacement;
    }

    private static BlockState getBlockStateForBucketItem(Item item) {
        if(item == Items.WATER_BUCKET) return Fluids.WATER.getDefaultState().getBlockState();
        if(item == Items.LAVA_BUCKET) return Fluids.LAVA.getDefaultState().getBlockState();
        return Blocks.AIR.getDefaultState();
    }

    public static ItemUsageContext getUseOnContext(HitResult hitResult, Item placingItem, BlockPos goal, ServerWorld world, Colonist pawn) {
        if(hitResult instanceof BlockHitResult) {
            final var owner = ServerExtensionsKt.getFortressOwner(pawn.getServer(), pawn.getFortressPos());
            final var randomPlayer = Optional.ofNullable(owner).orElse(pawn.getServer().getOverworld().getRandomAlivePlayer());
            final BlockHitResult movedHitResult = moveHitResult((BlockHitResult)hitResult,  goal);
            return new FortressUseOnContext(
                    world,
                    randomPlayer,
                    Hand.MAIN_HAND,
                    new ItemStack(placingItem),
                    movedHitResult
            );
        }

        return null;
    }

    private static boolean isItemBucket(Item item) {
        return item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET || item instanceof BucketItem;
    }


    private static ItemPlacementContext getBlockPlaceContext(HitResult hitResult, Direction horizontalDirection, Item placingItem, BlockPos goal, Colonist pawn) {
        if(hitResult instanceof BlockHitResult) {
            final var owner = ServerExtensionsKt.getFortressOwner(pawn.getServer(), pawn.getFortressPos());
            final var randomPlayer = Optional.ofNullable(owner).orElse(pawn.getServer().getOverworld().getRandomAlivePlayer());
            final BlockHitResult movedHitResult = moveHitResult((BlockHitResult) hitResult, goal);
            if(horizontalDirection != null){
                return new FortressBlockPlaceContext(
                        randomPlayer,
                        Hand.MAIN_HAND,
                        new ItemStack(placingItem, 64),
                        movedHitResult,
                        horizontalDirection
                );
            } else {
                return new ItemPlacementContext(
                        randomPlayer,
                        Hand.MAIN_HAND,
                        new ItemStack(placingItem, 64),
                        movedHitResult
                );
            }
        } else {
            return null;
        }
    }

    private static BlockHitResult moveHitResult(BlockHitResult blockHitResult, BlockPos goal) {
        final BlockPos clickedPos = goal.offset(blockHitResult.getSide().getOpposite()).toImmutable();
        return blockHitResult.withBlockPos(clickedPos);
    }

}
