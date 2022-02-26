package org.minefortress.blueprints.manager;

import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

import java.util.*;
import java.util.stream.Collectors;

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

//    public static BlueprintMetadata getByFile(String file) {
//        for(BlueprintMetadata info : STRUCTURES) {
//            if(info.getFile().equals(file)) {
//                return info;
//            }
//        }
//        return null;
//    }

    public List<BlueprintMetadata> getAllForGroup(BlueprintGroup group) {
        return blueprintsMap.getOrDefault(group, Collections.emptyList());
    }

    public BlueprintMetadata add(BlueprintGroup group, String name, String file, int floorLevel) {
        if (isContainsBlueprint(name, file)) {
            throw new IllegalArgumentException("Blueprint with name " + name + " and file " + file + " already exists");
        }

        final BlueprintMetadata metadata = new BlueprintMetadata(name, file, floorLevel);
        blueprintsMap.computeIfAbsent(group, k -> new ArrayList<>()).add(metadata);
        return metadata;
    }

    public void reset() {
        this.blueprintsMap.clear();
        this.index = 0;
    }

    public void update(String fileName, int newFloorLevel) {
        flatBlueprints()
                .stream()
                .filter(b -> b.getFile().equals(fileName))
                .forEach(b -> b.setFloorLevel(newFloorLevel));
    }

    private boolean isContainsBlueprint(String name, String file) {
        return flatBlueprints().stream().anyMatch(b -> b.getName().equals(name) && b.getFile().equals(file));
    }

    private List<BlueprintMetadata> flatBlueprints() {
        return blueprintsMap.entrySet().stream().flatMap(e -> e.getValue().stream()).collect(Collectors.toList());
    }

}
