package org.minefortress.blueprints.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.blueprints.data.BlueprintBlockData;
import org.minefortress.blueprints.data.ClientBlueprintBlockDataManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlueprintsModelBuilder {

    private final BlockBufferBuilderStorage blockBufferBuilders;
    private final ClientBlueprintBlockDataManager blockDataManager;

    private final Map<String, BuiltBlueprint> builtBlueprints = new HashMap<>();
    private final HashSet<BuiltBlueprint> blueprintsToClose = new HashSet<>();

    public BlueprintsModelBuilder(BlockBufferBuilderStorage blockBufferBuilders,
                                  ClientBlueprintBlockDataManager blockDataManager) {
        this.blockBufferBuilders = blockBufferBuilders;
        this.blockDataManager = blockDataManager;
    }

    public BuiltBlueprint getOrBuildBlueprint(String fileName, BlockRotation rotation) {
        buildBlueprint(fileName, rotation);

        return this.builtBlueprints.get(getKey(fileName, rotation));
    }

    public void buildBlueprint(String fileName, BlockRotation rotation) {
        for(BuiltBlueprint blueprint : this.blueprintsToClose) {
            blueprint.close();
        }

        String key = getKey(fileName, rotation);
        if(!this.builtBlueprints.containsKey(key)) {
            final BlueprintBlockData blockData = this.blockDataManager.getBlockData(fileName, rotation);
            final BuiltBlueprint builtBlueprint = new BuiltBlueprint(blockData, (p, c) -> getWorld().getColor(getBlockPos(), c));
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
            this.blueprintsToClose.add(blueprint);
            this.builtBlueprints.remove(key);
        }
    }

    public void reset() {
        this.blueprintsToClose.addAll(this.builtBlueprints.values());
        this.builtBlueprints.clear();
    }

    public ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }

    public ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    private BlockPos getBlockPos() {
        return getPlayer()!=null? getPlayer().getBlockPos():getWorld().getSpawnPos();
    }

}
