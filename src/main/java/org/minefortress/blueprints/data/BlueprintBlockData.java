package org.minefortress.blueprints.data;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.fortress.resources.ItemInfo;
import org.spongepowered.include.com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

public final class BlueprintBlockData {

    public static final Set<Item> IGNORED_ITEMS = Sets.newHashSet(
            Items.AIR,
            Items.STRUCTURE_VOID,
            Items.BARRIER,
            Items.DIRT,
            Items.GRASS_BLOCK,
            Items.DIRT_PATH,
            Items.GLASS,
            Items.GLASS_PANE,
            Items.WHITE_BED,
            Items.ORANGE_BED,
            Items.MAGENTA_BED,
            Items.LIGHT_BLUE_BED,
            Items.YELLOW_BED,
            Items.LIME_BED,
            Items.PINK_BED,
            Items.GRAY_BED,
            Items.LIGHT_GRAY_BED,
            Items.CYAN_BED,
            Items.PURPLE_BED,
            Items.BLUE_BED,
            Items.BROWN_BED,
            Items.GREEN_BED,
            Items.RED_BED,
            Items.BLACK_BED,
            Items.GRASS,
            Items.TALL_GRASS,
            Items.ACACIA_LEAVES,
            Items.BIRCH_LEAVES,
            Items.DARK_OAK_LEAVES,
            Items.JUNGLE_LEAVES,
            Items.OAK_LEAVES,
            Items.SPRUCE_LEAVES
    );

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

            final var layerBlockByItems = instance.layers.containsKey(BlueprintDataLayer.GENERAL) ?
                    instance.layers
                            .get(BlueprintDataLayer.GENERAL)
                            .values()
                            .stream()
                            .collect(Collectors.collectingAndThen(Collectors.groupingBy(it -> it.getBlock().asItem(), Collectors.counting()), Collections::unmodifiableMap))
                    :
                    instance.layers
                            .values()
                            .stream()
                            .flatMap(it -> it.values().stream())
                            .collect(Collectors.collectingAndThen(Collectors.groupingBy(it -> it.getBlock().asItem(), Collectors.counting()), Collections::unmodifiableMap));


            instance.stacks = layerBlockByItems.entrySet()
                    .stream()
                    .filter(it -> it.getValue() > 0 && !IGNORED_ITEMS.contains(it.getKey()))
                    .map(it -> new ItemInfo(it.getKey(), (int)(long)it.getValue()))
                    .toList();

            return instance;
        }
    }

}
