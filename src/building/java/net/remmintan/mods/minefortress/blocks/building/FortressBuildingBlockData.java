package net.remmintan.mods.minefortress.blocks.building;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.LogManager;

import java.util.*;

class FortressBuildingBlockData {

    private static final List<Block> IGNORED_BLOCKS = Arrays.asList(
            Blocks.STRUCTURE_BLOCK,
            Blocks.STRUCTURE_VOID,
            Blocks.AIR
    );
    private static final List<TagKey<Block>> TAGS = List.of(
            BlockTags.DIRT,
            BlockTags.SAND,
            BlockTags.LOGS
    );
    private final List<PositionedState> referenceState = new ArrayList<>();
    private final Map<BlockPos, BuildingBlockState> actualState = new HashMap<>();
    private int blockPointer = 0;
    private List<BlockPos> preservedPositions;


    FortressBuildingBlockData(Map<BlockPos, BlockState> preservedState, int floorYLevel) {
        for (Map.Entry<BlockPos, BlockState> entry : preservedState.entrySet()) {
            final var pos = entry.getKey();
            final var state = entry.getValue();
            if (shouldSkipBlock(pos, state, floorYLevel) || shouldSkipState(state))
                continue;
            final var positionedState = new PositionedState(pos, state);
            this.referenceState.add(positionedState);
            this.actualState.put(pos, BuildingBlockState.PRESERVED);
        }
    }

    public List<PositionedState> getReferenceState() {
        return referenceState;
    }

    private FortressBuildingBlockData(NbtCompound tag) {
        if (tag.contains("pointer", NbtType.NUMBER))
            blockPointer = tag.getInt("pointer");

        final var skippedPositions = new ArrayList<BlockPos>();
        if (tag.contains("referenceState", NbtType.LIST)) {
            final var list = tag.getList("referenceState", NbtType.COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                final var compound = list.getCompound(i);
                final var pos = BlockPos.fromLong(compound.getLong("pos"));
                final var blockStateTag = compound.get("blockState");
                if (blockStateTag != null) {
                    final BlockState blockState;
                    if (blockStateTag.getType() == NbtType.INT) {
                        final var nbtInt = (NbtInt) blockStateTag;
                        final var blockId = nbtInt.intValue();
                        final var block = Registries.BLOCK.get(blockId);
                        blockState = block.getDefaultState();
                    } else if (blockStateTag.getType() == NbtType.COMPOUND) {
                        final var compoundTag = (NbtCompound) blockStateTag;
                        blockState = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), compoundTag);
                    } else {
                        throw new IllegalArgumentException("Invalid block state tag");
                    }

                    if (shouldSkipState(blockState)) {
                        skippedPositions.add(pos);
                        continue;
                    }


                    final var positionedState = new PositionedState(pos, blockState);
                    referenceState.add(positionedState);
                }
            }
        }

        if (tag.contains("actualState", NbtType.LIST)) {
            final var list = tag.getList("actualState", NbtType.COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                final var compound = list.getCompound(i);
                final var pos = BlockPos.fromLong(compound.getLong("pos"));
                if (skippedPositions.contains(pos))
                    continue;
                final var blockState = compound.getString("blockState");
                try {
                    final var block = BuildingBlockState.valueOf(blockState);
                    actualState.put(pos, block);
                } catch (IllegalArgumentException e) {
                    LogManager.getLogger().error("Invalid block state: " + blockState);
                    throw e;
                }

            }
        }

        recalculatePreservedPositions();
    }

    private static boolean shouldSkipState(BlockState state) {
        for (Block ignoredBlock : IGNORED_BLOCKS) {
            if (state.isOf(ignoredBlock))
                return true;
        }

        return false;
    }

    private static boolean shouldSkipBlock(BlockPos pos, BlockState state, int floorYLevel) {
        if (state.isAir() || !state.getFluidState().isEmpty() || state.getBlock() == Blocks.STRUCTURE_VOID)
            return true;

        if (state.getBlock() == Blocks.STRUCTURE_BLOCK)
            return true;

        if (pos.getY() < floorYLevel) {
            return state.isIn(BlockTags.DIRT);
        }

        return false;
    }

    private static boolean areBlocksSimilar(BlockState block, BlockState actualBlock) {
        if (Objects.equals(block.getBlock(), actualBlock.getBlock()))
            return true;

        for (TagKey<Block> tag : TAGS) {
            if (blockAreInTheSameBlockTag(block, actualBlock, tag))
                return true;
        }

        return false;
    }

    private static boolean blockAreInTheSameBlockTag(BlockState a, BlockState b, TagKey<Block> blockTag) {
        return a.isIn(blockTag) && b.isIn(blockTag);
    }

    static FortressBuildingBlockData fromNbt(NbtCompound compound) {
        return new FortressBuildingBlockData(compound);
    }

    boolean checkTheNextBlocksState(int blocksAmount, ServerWorld world) {
        if (referenceState.isEmpty()) return false;
        if (world.getRegistryKey() != World.OVERWORLD)
            throw new IllegalArgumentException("The world must be the overworld");

        boolean stateUpdated = false;
        for (int i = 0; i < blocksAmount; i++) {
            blockPointer = blockPointer % referenceState.size();
            final var state = referenceState.get(blockPointer);
            final var pos = state.pos;
            final var referenceBlock = state.blockState;

            final var actualBlock = world.getBlockState(pos);

            final var previousState = actualState.getOrDefault(pos, BuildingBlockState.PRESERVED);
            final var newState = areBlocksSimilar(referenceBlock, actualBlock) ? BuildingBlockState.PRESERVED : BuildingBlockState.DESTROYED;

            actualState.put(pos, newState);

            blockPointer++;
            stateUpdated = stateUpdated || previousState != newState;
        }

        if (stateUpdated)
            recalculatePreservedPositions();

        return stateUpdated;
    }

    private void recalculatePreservedPositions() {
        preservedPositions = actualState.entrySet()
                .stream()
                .filter(it -> it.getValue() == BuildingBlockState.PRESERVED)
                .map(Map.Entry::getKey)
                .toList();
    }

    int getHealth() {
        if (actualState.isEmpty()) return 0;
        final var preserved = actualState.values().stream().filter(state -> state == BuildingBlockState.PRESERVED).count();
        final var delta = (float) preserved / (float) actualState.size();
        return (int) MathHelper.clampedMap(delta, 0.5f, 1, 0, 100);
    }

    NbtCompound toNbt() {
        final var tag = new NbtCompound();
        final var preservedStateList = new NbtList();
        for (PositionedState positionedState : referenceState) {
            final var compound = new NbtCompound();
            compound.putLong("pos", positionedState.pos.asLong());
            final var blockState = positionedState.blockState;
            compound.put("blockState", NbtHelper.fromBlockState(blockState));
            preservedStateList.add(compound);
        }
        tag.put("referenceState", preservedStateList);

        final var actualStateList = new NbtList();
        for (Map.Entry<BlockPos, BuildingBlockState> entry : actualState.entrySet()) {
            final var compound = new NbtCompound();
            compound.putLong("pos", entry.getKey().asLong());
            compound.putString("blockState", entry.getValue().name());
            actualStateList.add(compound);
        }

        tag.put("actualState", actualStateList);
        tag.putInt("pointer", blockPointer);

        return tag;
    }

    boolean attack(HostileEntity attacker) {
        final var world = attacker.getWorld();
        final var random = world.random;
        for (Map.Entry<BlockPos, BuildingBlockState> entries : actualState.entrySet()) {
            final var pos = entries.getKey();
            final var state = entries.getValue();
            if (state == BuildingBlockState.DESTROYED)
                continue;


            if (random.nextFloat() >= 0.6f) {
                world.syncWorldEvent(
                        WorldEvents.BLOCK_BROKEN,
                        pos,
                        Block.getRawIdFromState(world.getBlockState(pos))
                );
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                world.emitGameEvent(attacker, GameEvent.BLOCK_DESTROY, pos);
                return true;
            }
            break;
        }

        if (preservedPositions != null) {
            final var pos = preservedPositions.get(random.nextInt(preservedPositions.size()));
            world.setBlockBreakingInfo(attacker.getId(), pos, random.nextInt(10));
        }

        return false;
    }

    Map<BlockPos, BlockState> getAllBlockStatesToRepairTheBuilding() {
        final var map = new HashMap<BlockPos, BlockState>();
        for (Map.Entry<BlockPos, BuildingBlockState> entry : actualState.entrySet()) {
            final var pos = entry.getKey();
            final var state = entry.getValue();
            if (state == BuildingBlockState.PRESERVED)
                continue;

            final var blockState = referenceState
                    .stream()
                    .filter(it -> it.pos.equals(pos))
                    .findFirst()
                    .orElseThrow()
                    .blockState;
            map.put(pos, blockState);
        }
        return map;
    }

    List<BlockPos> getAllPresevedBlockPositions() {
        return preservedPositions;
    }

    List<BlockPos> getActualState() {
        return new ArrayList<>(actualState.keySet());
    }

    private enum BuildingBlockState {
        DESTROYED,
        PRESERVED,
    }

    public record PositionedState(BlockPos pos, BlockState blockState) {
    }

}
