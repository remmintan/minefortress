package org.minefortress.blueprints.renderer;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.util.BlockRotation;
import org.jetbrains.annotations.NotNull;
import org.minefortress.blueprints.data.BlueprintBlockData;
import org.minefortress.blueprints.data.ClientBlueprintBlockDataManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlueprintsModelBuilder {

    private final BlockBufferBuilderStorage blockBufferBuilders;
    private final ClientBlueprintBlockDataManager blockDataManager;

    private final Map<String, BuiltBlueprint> builtBlueprints = new HashMap<>();

    public BlueprintsModelBuilder(BlockBufferBuilderStorage blockBufferBuilders, ClientBlueprintBlockDataManager blockDataManager) {
        this.blockBufferBuilders = blockBufferBuilders;
        this.blockDataManager = blockDataManager;
    }

    public BuiltBlueprint getOrBuildBlueprint(String fileName, BlockRotation rotation) {
        buildBlueprint(fileName, rotation);

        return this.builtBlueprints.get(getKey(fileName, rotation));
    }

    public void buildBlueprint(String fileName, BlockRotation rotation) {
        String key = getKey(fileName, rotation);
        if(!this.builtBlueprints.containsKey(key)) {
            final BlueprintBlockData blockData = this.blockDataManager.getBlockData(fileName, rotation);
            final BuiltBlueprint builtBlueprint = new BuiltBlueprint(blockData);
            builtBlueprint.build(this.blockBufferBuilders);
            this.builtBlueprints.put(key, builtBlueprint);
        }
    }

    @NotNull
    private String getKey(String fileName, BlockRotation rotation) {
        return fileName + rotation.name();
    }

    public void invalidateBlueprint(final String fileName) {
        final List<Map.Entry<String, BuiltBlueprint>> toRemove = builtBlueprints.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(fileName))
                .collect(Collectors.toList());
        for(Map.Entry<String, BuiltBlueprint> entry : toRemove) {
            String key = entry.getKey();
            BuiltBlueprint blueprint = entry.getValue();
            blueprint.close();
            this.builtBlueprints.remove(key);
        }
    }

    public void reset() {
        this.builtBlueprints.values().forEach(BuiltBlueprint::close);
        this.builtBlueprints.clear();
    }

}
