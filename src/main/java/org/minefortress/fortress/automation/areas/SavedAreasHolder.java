package org.minefortress.fortress.automation.areas;

import net.minecraft.util.math.BlockPos;
import org.minefortress.selections.ClientSelection;
import org.minefortress.selections.renderer.tasks.ITasksModelBuilderInfoProvider;
import org.minefortress.selections.renderer.tasks.ITasksRenderInfoProvider;
import org.minefortress.utils.BuildingHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SavedAreasHolder implements ITasksModelBuilderInfoProvider, ITasksRenderInfoProvider {

    private boolean needsUpdate = true;
    private List<AutomationAreaInfo> savedAreas = Collections.emptyList();

    public void setSavedAreas(List<AutomationAreaInfo> savedAreas) {
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
    public Set<ClientSelection> getAllSelections() {
        return savedAreas.stream()
                .map(this::toClientSelection)
                .collect(Collectors.toSet());
    }

    public Optional<AutomationAreaInfo> getHovered(BlockPos pos) {
        return savedAreas.stream().filter(it -> it.contains(pos)).findAny();
    }

    @Override
    public boolean shouldRender() {
        return !savedAreas.isEmpty();
    }

    private ClientSelection toClientSelection(AutomationAreaInfo info) {
        return new ClientSelection(
                info.area(),
                info.areaType().getColor(),
                BuildingHelper::canRemoveBlock
        );
    }
}
