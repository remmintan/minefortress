package org.minefortress.blueprints.data;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StructureBlockData implements IStructureBlockData {

    private final Vec3i size;
    private final Map<BlueprintDataLayer, Map<BlockPos, BlockState>> layers = new HashMap<>();
    private List<IItemInfo> stacks;

    private StructureBlockData(Vec3i size) {
        this.size = size;
    }

    @Override
    public boolean hasLayer(BlueprintDataLayer layer) {
        return layers.containsKey(layer);
    }

    @Override
    public Map<BlockPos, BlockState> getLayer(BlueprintDataLayer layer) {
        return layers.get(layer);
    }

    @Override
    public Vec3i getSize() {
        return size;
    }

    @Override
    public List<IItemInfo> getStacks() {
        return stacks;
    }

    static Builder withBlueprintSize(Vec3i blueprintSize) {
        return new Builder(blueprintSize);
    }

    static final class Builder {

        private final StructureBlockData instance;

        private Builder(Vec3i blueprintSize) {
            instance = new StructureBlockData(blueprintSize);
        }

        Builder setLayer(BlueprintDataLayer layer, Map<BlockPos, BlockState> layerData) {
            instance.layers.put(layer, Collections.unmodifiableMap(layerData));
            return this;
        }

        IStructureBlockData build() {
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
                    .filter(it -> it.getValue() > 0 && !SimilarItemsHelper.isIgnorable(it.getKey()))
                    .map(this::getItemInfo)
                    .toList();

            return instance;
        }

        @NotNull
        private IItemInfo getItemInfo(Map.Entry<Item, Long> it) {
            return new ItemInfo(it.getKey(), getItemAmount(it));
        }

        private int getItemAmount(Map.Entry<Item, Long> entry) {
            final var defaultStack = entry.getKey().getDefaultStack();
            final var count = entry.getValue().intValue();

            final var shouldBeDivided = defaultStack.isIn(ItemTags.BEDS) || defaultStack.isIn(ItemTags.DOORS);

            return shouldBeDivided ? count / 2 : count;
        }
    }

}
