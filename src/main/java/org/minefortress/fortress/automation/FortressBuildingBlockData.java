package org.minefortress.fortress.automation;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class FortressBuildingBlockData {

    private final Map<BlockPos, Block> data = new HashMap<>();

    FortressBuildingBlockData(Map<BlockPos, BlockState> data) {
        for (Map.Entry<BlockPos, BlockState> entry : data.entrySet()) {
            final var pos = entry.getKey();
            final var state = entry.getValue();
            this.data.put(pos, state.getBlock());
        }
    }

    private FortressBuildingBlockData(NbtList list) {
        for (int i = 0; i < list.size(); i++) {
            final var compound = list.getCompound(i);
            final var pos = BlockPos.fromLong(compound.getLong("pos"));
            final var block = Registry.BLOCK.get(compound.getInt("block"));
            data.put(pos, block);
        }
    }

    NbtElement toNbt() {
        final var list = new NbtList();
        for (Map.Entry<BlockPos, Block> entry : data.entrySet()) {
            final var pos = entry.getKey();
            final var block = entry.getValue();
            final var compound = new NbtCompound();
            compound.putLong("pos", pos.asLong());
            compound.putInt("block", Registry.BLOCK.getRawId(block));
            list.add(compound);
        }
        return list;
    }

    static FortressBuildingBlockData fromNbt(@Nullable NbtElement list) {
        if(list == null || list.getType() != NbtType.LIST)
            throw new IllegalArgumentException("NbtElement must be a NbtList");
        return new FortressBuildingBlockData((NbtList) list);
    }

}
