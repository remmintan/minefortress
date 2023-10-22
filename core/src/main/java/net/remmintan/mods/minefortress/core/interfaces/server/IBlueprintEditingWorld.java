package net.remmintan.mods.minefortress.core.interfaces.server;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;

public interface IBlueprintEditingWorld {
    String getFileName();

    void setFileName(String fileName);

    int getFloorLevel();

    void setFloorLevel(int floorLevel);

    BlueprintGroup getBlueprintGroup();

    void setBlueprintGroup(BlueprintGroup blueprintGroup);

    void enableSaveStructureMode();

    void disableSaveStructureMode();
}
