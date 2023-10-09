package org.minefortress.fortress.automation;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.AutomationActionType;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;

public record AutomationBlockInfo(
        BlockPos pos,
        AutomationActionType info
) implements IAutomationBlockInfo {}
