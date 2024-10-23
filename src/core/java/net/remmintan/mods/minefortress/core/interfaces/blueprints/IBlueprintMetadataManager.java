package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import java.util.List;
import java.util.Optional;

public interface IBlueprintMetadataManager {
    void select(IBlueprintMetadata metadata);

    IBlueprintMetadata selectNext();

    default List<IBlueprintMetadata> getAllForGroup(BlueprintGroup group) {
        return getAllForGroup(group, 0);
    }

    List<IBlueprintMetadata> getAllForGroup(BlueprintGroup group, Integer level);

    IBlueprintMetadata add(BlueprintGroup group, String name, String blueprintId, int floorLevel);

    void reset();

    void remove(String filename);

    void update(String fileName, int newFloorLevel);

    Optional<IBlueprintMetadata> getByBlueprintId(String blueprintId);
}
