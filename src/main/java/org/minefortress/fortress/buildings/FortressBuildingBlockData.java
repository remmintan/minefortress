package org.minefortress.fortress.buildings;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

class FortressBuildingBlockData {

    private int blockPointer = 0;
    private final Map<BlockPos, Block> data = new HashMap<>();

    FortressBuildingBlockData(Map<BlockPos, BlockState> data) {
        for (Map.Entry<BlockPos, BlockState> entry : data.entrySet()) {
            final var pos = entry.getKey();
            final var state = entry.getValue();
            final var block = state.getBlock();
            if(block == Blocks.AIR)
                continue;
            this.data.put(pos, block);
        }
    }

    private FortressBuildingBlockData(NbtCompound tag) {
        if(tag.contains("pointer", NbtType.NUMBER))
            blockPointer = tag.getInt("pointer");

        if(tag.contains("blocks", NbtType.LIST)) {
            final var list = tag.getList("blocks", NbtType.COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                final var compound = list.getCompound(i);
                final var pos = BlockPos.fromLong(compound.getLong("pos"));
                final var block = Registry.BLOCK.get(compound.getInt("block"));
                data.put(pos, block);
            }
        }
    }

    NbtCompound toNbt() {
        final var tag = new NbtCompound();
        final var list = new NbtList();
        for (Map.Entry<BlockPos, Block> entry : data.entrySet()) {
            final var pos = entry.getKey();
            final var block = entry.getValue();
            final var compound = new NbtCompound();
            compound.putLong("pos", pos.asLong());
            compound.putInt("block", Registry.BLOCK.getRawId(block));
            list.add(compound);
        }

        tag.put("blocks", list);
        tag.putInt("pointer", blockPointer);

        return tag;
    }

    static FortressBuildingBlockData fromNbt(NbtCompound compound) {
        return new FortressBuildingBlockData(compound);
    }

}
