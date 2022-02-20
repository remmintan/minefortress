package org.minefortress.blueprints.renderer;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.util.BlockRotation;
import org.jetbrains.annotations.NotNull;
import org.minefortress.blueprints.BlueprintBlockDataManager;
import org.minefortress.interfaces.FortressMinecraftClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BlueprintsModelBuilder {

    private final BlockBufferBuilderStorage blockBufferBuilders;
    private final BlueprintBlockDataManager blockDataManager;

    private final Map<String, BuiltBlueprint> builtBlueprints = new HashMap<>();

    public BlueprintsModelBuilder(BufferBuilderStorage bufferBuilders, FortressMinecraftClient client) {
        this.blockBufferBuilders = bufferBuilders.getBlockBufferBuilders();
        this.blockDataManager = client.getBlueprintBlockDataManager();
    }

    public BuiltBlueprint getOrBuildBlueprint(String fileName, BlockRotation rotation) {
        buildBlueprint(fileName, rotation);

        return this.builtBlueprints.get(getKey(fileName, rotation));
    }

    public void buildBlueprint(String fileName, BlockRotation rotation) {
        String key = getKey(fileName, rotation);
        if(!this.builtBlueprints.containsKey(key)) {
            final BlueprintBlockDataManager.BlueprintBlockData blockData = this.blockDataManager.getBlockData(fileName, rotation, false);
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
        new HashSet<>(builtBlueprints.keySet()).stream().filter(key -> key.startsWith(fileName)).forEach(builtBlueprints::remove);
    }

}
