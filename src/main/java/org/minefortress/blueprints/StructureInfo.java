package org.minefortress.blueprints;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class StructureInfo {

    private final String name;
    private final String file;
    private final int roofHeight;
    private final List<Block> roofBlocks = new ArrayList<>();

    private BlockRotation rotation = BlockRotation.NONE;

    public StructureInfo(String name, String file) {
        this.name = name;
        this.file = file;
        this.roofHeight = 0;
    }

    public StructureInfo(String name, String file, int roofHeight, List<Block> roofBlocks) {
        this.name = name;
        this.file = file;
        this.roofHeight = roofHeight;
        this.roofBlocks.addAll(roofBlocks);
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getId() {
        return file +"-"+rotation.name();
    }

    public void rotateRight() {
        rotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
    }

    public void rotateLeft() {
        rotation = rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
    }

    public BlockRotation getRotation() {
        return rotation;
    }

    public boolean isPartOfAutomaticLayer(BlockPos pos, BlockState state) {
        if(pos.getY() < this.roofHeight) return false;
        final Block block = state.getBlock();
        return roofBlocks.contains(block);
    }

}
