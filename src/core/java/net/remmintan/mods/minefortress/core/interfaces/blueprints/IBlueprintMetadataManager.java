package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata;

import java.util.List;
import java.util.Optional;

public interface IBlueprintMetadataManager {
    void select(IBlueprintMetadata metadata);

    IBlueprintMetadata selectNext();

    List<IBlueprintMetadata> getAllForGroup(BlueprintGroup group);

    IBlueprintMetadata add(BlueprintGroup group, String name, String file, int floorLevel, String requirementId);

    void reset();

    void remove(String filename);

    void update(String fileName, int newFloorLevel);

    Optional<IBlueprintMetadata> getByBlueprintId(String blueprintId);
}
