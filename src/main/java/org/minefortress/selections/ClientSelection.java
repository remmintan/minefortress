package org.minefortress.selections;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClientSelection {

    private final UUID id;
    private final SelectionType selectionType;
    private final Set<BlockPos> blockPositions;
    private final BlockState buildingBlockState;

    public ClientSelection(UUID id, SelectionType selectionType, Iterable<BlockPos> blockPositions, BlockState buildingBlock) {
        this.id = id;
        this.selectionType = selectionType;
        HashSet<BlockPos> positions = new HashSet<>();
        for(BlockPos pos: blockPositions) {
            positions.add(pos.toImmutable());
        }
        this.blockPositions = positions;
        this.buildingBlockState = buildingBlock;
    }

    public UUID getId() {
        return id;
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public Set<BlockPos> getBlockPositions() {
        return blockPositions;
    }

    public BlockState getBuildingBlockState() {
        return buildingBlockState;
    }
}
