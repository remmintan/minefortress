package org.minefortress.blueprints.manager;

import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadataManager;

import java.util.*;

public final class BlueprintMetadataManager implements IBlueprintMetadataManager {

    private final Map<BlueprintGroup, List<BlueprintMetadata>> blueprintsGroups = new HashMap<>();

    @Override
    public List<BlueprintMetadata> getAllForGroup(BlueprintGroup group) {
        return blueprintsGroups
                .getOrDefault(group, Collections.emptyList())
                .stream()
                .filter(it -> it.getRequirement().getLevel() == 0)
                .toList();
    }

    @Override
    public void sync(BlueprintMetadata metadata) {
        final var groupId = metadata.getGroup();
        final var group = blueprintsGroups.computeIfAbsent(groupId, k -> new ArrayList<>());

        final var blueprintId = metadata.getId();
        group.removeIf(it -> it.getId().equals(blueprintId));

        group.add(metadata);
    }

    @Override
    public void reset() {
        this.blueprintsGroups.clear();
    }

    @Override
    public void remove(String blueprintId) {
        blueprintsGroups.forEach((k, v) -> v.removeIf(it -> it.getId().equals(blueprintId)));
    }

    private List<BlueprintMetadata> flatBlueprints() {
        return blueprintsGroups.values().stream().flatMap(Collection::stream).toList();
    }

    @Override
    public Optional<BlueprintMetadata> getByBlueprintId(String blueprintId) {
        if(blueprintId == null) return Optional.empty();
        return flatBlueprints().stream().filter(b -> b.getId().equals(blueprintId)).findFirst();
    }

}
