package org.minefortress.blueprints.manager;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;

import java.util.*;

public final class BlueprintMetadataManager {

    private final Map<BlueprintGroup, List<BlueprintMetadata>> blueprintsMap = new HashMap<>();
    private int index = 0;

    public void select(BlueprintMetadata metadata) {
        index = flatBlueprints().indexOf(metadata);
    }

    public BlueprintMetadata selectNext() {
        index++;
        if (index >= flatBlueprints().size()) {
            index = 0;
        }
        return flatBlueprints().get(index);
    }

    public List<BlueprintMetadata> getAllForGroup(BlueprintGroup group) {
        return blueprintsMap.getOrDefault(group, Collections.emptyList());
    }

    public BlueprintMetadata add(BlueprintGroup group, String name, String file, int floorLevel, String requirementId) {
        if (isContainsBlueprint(name, file)) {
            throw new IllegalArgumentException("Blueprint with areaType " + name + " and file " + file + " already exists");
        }

        final BlueprintMetadata metadata = new BlueprintMetadata(name, file, floorLevel, requirementId);
        blueprintsMap.computeIfAbsent(group, k -> new ArrayList<>()).add(metadata);
        return metadata;
    }

    public void reset() {
        this.blueprintsMap.clear();
        this.index = 0;
    }

    public void remove(String filename) {
        blueprintsMap.forEach((k, v) -> v.removeIf(it -> it.getId().equals(filename)));
    }

    public void update(String fileName, int newFloorLevel) {
        flatBlueprints()
                .stream()
                .filter(b -> b.getId().equals(fileName))
                .forEach(b -> b.setFloorLevel(newFloorLevel));
    }

    private boolean isContainsBlueprint(String name, String file) {
        return flatBlueprints().stream().anyMatch(b -> b.getName().equals(name) && b.getId().equals(file));
    }

    private List<BlueprintMetadata> flatBlueprints() {
        return blueprintsMap.values().stream().flatMap(Collection::stream).toList();
    }

    public Optional<BlueprintMetadata> getByBlueprintId(String blueprintId) {
        if(blueprintId == null) return Optional.empty();
        return flatBlueprints().stream().filter(b -> b.getId().equals(blueprintId)).findFirst();
    }

}
