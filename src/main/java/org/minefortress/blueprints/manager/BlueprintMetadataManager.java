package org.minefortress.blueprints.manager;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadataManager;

import java.util.*;

public final class BlueprintMetadataManager implements IBlueprintMetadataManager {

    private final Map<BlueprintGroup, List<IBlueprintMetadata>> blueprintsMap = new HashMap<>();
    private int index = 0;

    @Override
    public void select(IBlueprintMetadata metadata) {
        index = flatBlueprints().indexOf(metadata);
    }

    @Override
    public IBlueprintMetadata selectNext() {
        index++;
        if (index >= flatBlueprints(0).size()) {
            index = 0;
        }
        return flatBlueprints(0).get(index);
    }


    @Override
    public List<IBlueprintMetadata> getAllForGroup(BlueprintGroup group, Integer level) {
        return blueprintsMap
                .getOrDefault(group, Collections.emptyList())
                .stream()
                .filter(it -> it.getLevel() == level)
                .toList();
    }

    @Override
    public IBlueprintMetadata add(BlueprintGroup group, String name, String file, int floorLevel, String requirementId) {
        if (isContainsBlueprint(name, file)) {
            throw new IllegalArgumentException("Blueprint with areaType " + name + " and file " + file + " already exists");
        }

        final IBlueprintMetadata metadata = new BlueprintMetadata(name, file, floorLevel, requirementId);
        blueprintsMap.computeIfAbsent(group, k -> new ArrayList<>()).add(metadata);
        return metadata;
    }

    @Override
    public void reset() {
        this.blueprintsMap.clear();
        this.index = 0;
    }

    @Override
    public void remove(String filename) {
        blueprintsMap.forEach((k, v) -> v.removeIf(it -> it.getId().equals(filename)));
    }

    @Override
    public void update(String fileName, int newFloorLevel) {
        flatBlueprints()
                .stream()
                .filter(b -> b.getId().equals(fileName))
                .forEach(b -> b.setFloorLevel(newFloorLevel));
    }

    private boolean isContainsBlueprint(String name, String file) {
        return flatBlueprints().stream().anyMatch(b -> b.getName().equals(name) && b.getId().equals(file));
    }

    private List<IBlueprintMetadata> flatBlueprints() {
        return blueprintsMap.values().stream().flatMap(Collection::stream).toList();
    }

    private List<IBlueprintMetadata> flatBlueprints(int level) {
        return blueprintsMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(it -> it.getLevel() == level)
                .toList();
    }

    @Override
    public Optional<IBlueprintMetadata> getByBlueprintId(String blueprintId) {
        if(blueprintId == null) return Optional.empty();
        return flatBlueprints().stream().filter(b -> b.getId().equals(blueprintId)).findFirst();
    }

}
