package net.remmintan.gobi;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionType;
import net.remmintan.mods.minefortress.core.utils.PathUtils;

import java.util.List;

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
    public List<BlockPos> getSelection() {
        return super.getSelection();
    }

    @Override
    protected ISelectionType getSelectionType() {
        return SelectionType.LADDER;
    }

    @Override
    protected List<BlockPos> getIterableForSelectionUpdate(BlockPos selectionStart, BlockPos selectionEnd) {
        return PathUtils.getLadderSelection(selectionStart, selectionEnd, getAxis());
    }

    protected Direction.Axis getAxis() {
        return Direction.Axis.X;
    }

    private int newDelta(BlockPos pickedBlock) {
        return selectionStart==null?0: PathUtils.findDeltaForAxis(selectionStart, pickedBlock, getAxis());
    }
}
