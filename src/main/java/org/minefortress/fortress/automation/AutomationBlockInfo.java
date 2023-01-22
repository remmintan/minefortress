package org.minefortress.fortress.automation;

import net.minecraft.util.math.BlockPos;

public record AutomationBlockInfo(
        BlockPos pos,
        AutomationActionType info
) {}
