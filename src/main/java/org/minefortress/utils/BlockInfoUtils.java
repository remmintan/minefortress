package org.minefortress.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.FortressBlockPlaceContext;
import org.minefortress.entity.ai.FortressUseOnContext;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlockInfoUtils {

    public static boolean shouldBePlacedAsItem(Item item) {
        if(item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET) return false;
        if(item instanceof TallBlockItem) return true;
        if(item instanceof BedItem) return true;
        if(!(item instanceof final BlockItem blockItem)) return true;


        final Block block = blockItem.getBlock();
        final boolean solid = block.getDefaultState().getMaterial().isSolid();
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

    public static ItemUsageContext getUseOnContext(HitResult hitResult, Item placingItem, BlockPos goal, ServerWorld world, Colonist colonist) {
        if(hitResult instanceof BlockHitResult) {
            ServerPlayerEntity masterPlayer = colonist
                    .getMasterPlayer()
                    .or(() -> Optional
                            .ofNullable(colonist.getServer())
                            .map(MinecraftServer::getOverworld)
                            .map(ServerWorld::getRandomAlivePlayer))
                    .orElseThrow(() -> new IllegalStateException("Colonist has no master player"));
            final BlockHitResult movedHitResult = moveHitResult((BlockHitResult)hitResult,  goal);
            return new FortressUseOnContext(
                    world,
                    masterPlayer,
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



    private static ItemPlacementContext getBlockPlaceContext(HitResult hitResult, Direction horizontalDirection, Item placingItem, BlockPos goal, Colonist colonist) {
        if(hitResult instanceof BlockHitResult) {
            ServerPlayerEntity randomPlayer = colonist.getMasterPlayer()
                    .or(() -> Optional
                            .ofNullable(colonist.getServer())
                            .map(MinecraftServer::getOverworld)
                            .map(ServerWorld::getRandomAlivePlayer))
                    .orElseThrow(() -> new IllegalStateException("Colonist has no master player"));
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

    @NotNull
    public static Map<Item, Long> convertBlockStatesMapItemsMap(Map<BlockPos, BlockState> blocksToRepair) {
        final var amountOfRequiredItems = blocksToRepair
                .entrySet()
                .stream()
                .collect(Collectors
                        .groupingBy(it -> it.getValue().getBlock().asItem(),
                                Collectors.counting()
                        )
                );

        for (Item item : Collections.unmodifiableSet(amountOfRequiredItems.keySet())) {
            if(item instanceof BlockItem blockItem) {
                final var defaultBlockState = blockItem.getBlock().getDefaultState();
                if(defaultBlockState.isIn(BlockTags.DOORS) || defaultBlockState.isIn(BlockTags.BEDS)) {
                    amountOfRequiredItems.put(item, amountOfRequiredItems.get(item) / 2);
                }
            }
        }

        return amountOfRequiredItems;
    }

}
