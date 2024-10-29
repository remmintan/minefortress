package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;

import java.util.List;
import java.util.Optional;

public interface IBlueprintMetadataManager {

    List<BlueprintMetadata> getAllForGroup(BlueprintGroup group);

    void sync(BlueprintMetadata metadata);

    void reset();

    void remove(String blueprintId);

    Optional<BlueprintMetadata> getByBlueprintId(String blueprintId);
}
