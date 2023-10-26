package net.remmintan.mods.minefortress.core.interfaces.automation.area;

import net.minecraft.util.math.BlockPos;

public interface IAutomationBlockInfo {

    BlockPos pos();

    AutomationActionType info();

}
