package org.minefortress.blueprints.data;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BlueprintBlockData {

    private final Vec3i size;
    private final Map<BlueprintDataLayer, Map<BlockPos, BlockState>> layers = new HashMap<>();
    private List<ItemInfo> stacks;

    public BlueprintBlockData(Vec3i size) {
        this.size = size;
    }

    public boolean hasLayer(BlueprintDataLayer layer) {
        return layers.containsKey(layer);
    }

    public Map<BlockPos, BlockState> getLayer(BlueprintDataLayer layer) {
        return layers.get(layer);
    }

    public Vec3i getSize() {
        return size;
    }

    public List<ItemInfo> getStacks() {
        return stacks;
    }

    static Builder withBlueprintSize(Vec3i blueprintSize) {
        return new Builder(blueprintSize);
    }

    static final class Builder {

        private final BlueprintBlockData instance;

        private Builder(Vec3i blueprintSize) {
            instance = new BlueprintBlockData(blueprintSize);
        }

        Builder setLayer(BlueprintDataLayer layer, Map<BlockPos, BlockState> layerData) {
            instance.layers.put(layer, Collections.unmodifiableMap(layerData));
            return this;
        }

        BlueprintBlockData build() {
            instance.stacks = instance.layers
                    .values()
                    .stream()
                    .flatMap(it -> it.values().stream())
                    .collect(Collectors.collectingAndThen(Collectors.groupingBy(it -> it.getBlock().asItem(), Collectors.counting()), Collections::unmodifiableMap))
                    .entrySet()
                    .stream()
                    .map(it -> new ItemInfo(it.getKey(), (int)(long)it.getValue()))
                    .toList();

            return instance;
        }
    }

}
