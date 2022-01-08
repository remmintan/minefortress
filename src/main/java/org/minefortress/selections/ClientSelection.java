package org.minefortress.selections;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class ClientSelection {

    private final Set<BlockPos> blockPositions;
    private final BlockState buildingBlockState;

    public ClientSelection( Iterable<BlockPos> blockPositions, BlockState buildingBlock) {
        HashSet<BlockPos> positions = new HashSet<>();
        for(BlockPos pos: blockPositions) {
            positions.add(pos.toImmutable());
        }
        this.blockPositions = positions;
        this.buildingBlockState = buildingBlock;
    }

    public Set<BlockPos> getBlockPositions() {
        return blockPositions;
    }

    public BlockState getBuildingBlockState() {
        return buildingBlockState;
    }
}
