package net.remmintan.mods.minefortress.core.interfaces.renderers;

public interface IGuiBlueprintsRenderer {
    void renderBlueprintPreview(String blueprintId);

    void renderBlueprintSlot(String blueprintId, int column, int row, boolean isEnoughResources);

    void renderBlueprintUpgrade(String blueprintId, int number, boolean unlocked);
}
