package org.minefortress.fortress.automation.areas;

import net.minecraft.util.math.BlockPos;
import net.remmintan.gobi.ClientSelection;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ISavedAreasHolder;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksModelBuilderInfoProvider;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksRenderInfoProvider;
import net.remmintan.mods.minefortress.building.BuildingHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SavedAreasHolder implements ITasksModelBuilderInfoProvider, ITasksRenderInfoProvider, ISavedAreasHolder {

    private boolean needsUpdate = true;
    private List<IAutomationAreaInfo> savedAreas = Collections.emptyList();

    @Override
    public void setSavedAreas(List<IAutomationAreaInfo> savedAreas) {
        this.savedAreas = Collections.unmodifiableList(savedAreas);
        this.setNeedRebuild(true);
    }

    @Override
    public boolean isNeedRebuild() {
        return needsUpdate;
    }

    @Override
    public void setNeedRebuild(boolean rebuildNeeded) {
        this.needsUpdate = rebuildNeeded;
    }

    @Override
    public Set<IClientTask> getAllSelections() {
        return savedAreas.stream()
                .map(this::toClientSelection)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<IAutomationAreaInfo> getHovered(BlockPos pos) {
        return savedAreas.stream().filter(it -> it.contains(pos)).findAny();
    }

    @Override
    public boolean shouldRender() {
        return !savedAreas.isEmpty();
    }

    private IClientTask toClientSelection(IAutomationAreaInfo info) {
        return new ClientSelection(
                info.getClientArea(),
                info.getAreaType().getColor(),
                BuildingHelper::canRemoveBlock
        );
    }
}
