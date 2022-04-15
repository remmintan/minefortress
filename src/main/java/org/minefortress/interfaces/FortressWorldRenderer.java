package org.minefortress.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.minefortress.selections.ClickType;

import java.util.List;

public interface FortressWorldRenderer {

    @Nullable
    BlockState getClickingBlock();
    List<BlockPos> getSelectedBlocks();
    ClickType getClickType();

}
