package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;

import java.util.List;
import java.util.Optional;

public interface ISavedAreasHolder extends ITasksRenderInfoProvider, ITasksModelBuilderInfoProvider {
    void setSavedAreas(List<IAutomationAreaInfo> savedAreas);

    Optional<IAutomationAreaInfo> getHovered(BlockPos pos);

    void setNeedRebuild(boolean rebuildNeeded);
}
