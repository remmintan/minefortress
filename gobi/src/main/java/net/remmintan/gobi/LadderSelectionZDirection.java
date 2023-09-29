package net.remmintan.gobi;

import net.minecraft.util.math.Direction;

public class LadderSelectionZDirection extends LadderSelection{

    @Override
    protected Direction.Axis getAxis() {
        return Direction.Axis.Z;
    }

    @Override
    protected SelectionType getSelectionType() {
        return SelectionType.LADDER_Z_DIRECTION;
    }
}
