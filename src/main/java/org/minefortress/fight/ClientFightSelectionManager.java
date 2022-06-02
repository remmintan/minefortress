package org.minefortress.fight;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

public class ClientFightSelectionManager {

    private MousePos selectionStartPos;
    private BlockPos selectionStartBlock;
    private MousePos selectionCurPos;
    private BlockPos selectionCurBlock;

    public void startSelection(double x, double y, BlockPos startBlock) {
        this.selectionStartPos = new MousePos(x, y);
        this.selectionStartBlock = startBlock;
    }

    public void updateSelection(double x, double y, BlockPos endBlock) {
        this.selectionCurPos = new MousePos(x, y);
        this.selectionCurBlock = endBlock;
    }

    public void resetSelection() {
        this.selectionStartPos = null;
        this.selectionStartBlock = null;
        this.selectionCurPos = null;
        this.selectionCurBlock = null;
    }

    public boolean isSelecting() {
        return this.selectionStartPos != null && this.selectionStartBlock != null && this.selectionCurPos != null && this.selectionCurBlock != null;
    }

    public MousePos getSelectionStartPos() {
        return selectionStartPos;
    }

    public BlockPos getSelectionStartBlock() {
        return selectionStartBlock;
    }

    public MousePos getSelectionCurPos() {
        return selectionCurPos;
    }

    public BlockPos getSelectionCurBlock() {
        return selectionCurBlock;
    }

    public record MousePos(double x, double y) {
        public int getX() {
            return (int) x;
        }

        public int getY() {
            return (int) y;
        }
    }
}
