package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.util.hit.HitResult;
import net.remmintan.mods.minefortress.core.interfaces.automation.ProfessionsSelectionType;

import java.util.Optional;

public interface IAreasClientManager {
    boolean select(HitResult target);

    void updateSelection(HitResult crosshairTarget);

    void resetSelection();

    void removeHovered();

    ProfessionsSelectionType getSelectionType();

    void setSelectionType(ProfessionsSelectionType selectionType);

    ISavedAreasHolder getSavedAreasHolder();

    Optional<String> getHoveredAreaName();
}
