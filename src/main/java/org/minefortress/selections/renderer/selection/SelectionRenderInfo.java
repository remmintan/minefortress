package org.minefortress.selections.renderer.selection;


import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vector4f;
import org.minefortress.selections.ClickType;

import java.util.Collections;
import java.util.Set;

public class SelectionRenderInfo {

    private final ClickType clickType;
    private final Vector4f color;
    private final Set<BlockPos> positions;

    public SelectionRenderInfo(ClickType clickType, Vector4f color, Set<BlockPos> positions) {
        this.clickType = clickType;
        this.color = color;
        this.positions = Collections.unmodifiableSet(positions);
    }

    public ClickType getClickType() {
        return clickType;
    }

    public Vector4f getColor() {
        return new Vector4f(color.getX(), color.getY(), color.getZ(), color.getW());
    }

    public Set<BlockPos> getPositions() {
        return positions;
    }
}
