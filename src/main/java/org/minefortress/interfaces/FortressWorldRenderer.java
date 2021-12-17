package org.minefortress.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.minefortress.selections.ClickType;

import java.util.Set;

public interface FortressWorldRenderer {

    @Nullable
    BlockState getClickingBlock();
    Set<BlockPos> getSelectedBlocks();
    ClickType getClickType();

}
