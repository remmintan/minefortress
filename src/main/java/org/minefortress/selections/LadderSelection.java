package org.minefortress.selections;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static org.minefortress.utils.PathUtils.findDeltaForAxis;
import static org.minefortress.utils.PathUtils.getLadderSelection;

public class LadderSelection extends TwoDotsSelection{

    @Override
    public boolean needUpdate(BlockPos pickedBlock, int upSelectionDelta) {
        if(selectionStart != null) {
            pickedBlock = new BlockPos(pickedBlock.getX(), selectionStart.getY(), pickedBlock.getZ());
        }
        upSelectionDelta = newDelta(pickedBlock);
        return super.needUpdate(pickedBlock, upSelectionDelta);
    }

    @Override
    public void update(BlockPos pickedBlock, int upDelta) {
        if(selectionStart != null) {
            pickedBlock = new BlockPos(pickedBlock.getX(), selectionStart.getY(), pickedBlock.getZ());
        }
        upDelta = newDelta(pickedBlock);
        super.update(pickedBlock, upDelta);
    }

    @Override
    public Iterable<BlockPos> getSelection() {
        return super.getSelection();
    }

    @Override
    protected SelectionType getSelectionType() {
        return SelectionType.LADDER;
    }

    @Override
    protected Iterable<BlockPos> getIterableForSelectionUpdate(BlockPos selectionStart, BlockPos selectionEnd) {
        return getLadderSelection(selectionStart, selectionEnd, getAxis());
    }

    protected Direction.Axis getAxis() {
        return Direction.Axis.X;
    }

    private int newDelta(BlockPos pickedBlock) {
        return selectionStart==null?0:findDeltaForAxis(selectionStart, pickedBlock, getAxis());
    }
}
