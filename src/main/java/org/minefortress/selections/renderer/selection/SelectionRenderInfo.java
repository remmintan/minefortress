package org.minefortress.selections.renderer.selection;


import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vector4f;
import org.minefortress.selections.ClickType;

import java.util.Collections;
import java.util.List;

public class SelectionRenderInfo {

    private final ClickType clickType;
    private final Vector4f color;
    private final List<BlockPos> positions;
    private final BlockState blockState;

    public SelectionRenderInfo(ClickType clickType, Vector4f color, List<BlockPos> positions, BlockState blockState) {
        this.clickType = clickType;
        this.color = color;
        this.positions = Collections.unmodifiableList(positions);
        this.blockState = blockState;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public Vector4f getColor() {
        return new Vector4f(color.getX(), color.getY(), color.getZ(), color.getW());
    }

    public List<BlockPos> getPositions() {
        return positions;
    }

    public BlockState getBlockState() {
        return blockState;
    }
}
