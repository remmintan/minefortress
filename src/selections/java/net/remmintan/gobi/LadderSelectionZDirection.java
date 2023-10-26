package net.remmintan.gobi;

import net.minecraft.util.math.Direction;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionType;

public class LadderSelectionZDirection extends LadderSelection{

    @Override
    protected Direction.Axis getAxis() {
        return Direction.Axis.Z;
    }

    @Override
    protected ISelectionType getSelectionType() {
        return SelectionType.LADDER_Z_DIRECTION;
    }
}
