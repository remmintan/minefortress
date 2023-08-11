package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import org.minefortress.blueprints.data.StrctureBlockData;

import java.util.*;

public class SimilarItemsHelper {

    private static final List<Item> strippedLogs = Arrays.asList(
            Items.STRIPPED_ACACIA_LOG,
            Items.STRIPPED_BIRCH_LOG,
            Items.STRIPPED_DARK_OAK_LOG,
            Items.STRIPPED_JUNGLE_LOG,
            Items.STRIPPED_OAK_LOG,
            Items.STRIPPED_SPRUCE_LOG,
            Items.STRIPPED_WARPED_STEM,
            Items.STRIPPED_CRIMSON_STEM
    );

    private static final List<Item> strippedWood = Arrays.asList(
            Items.STRIPPED_ACACIA_WOOD,
            Items.STRIPPED_BIRCH_WOOD,
            Items.STRIPPED_DARK_OAK_WOOD,
            Items.STRIPPED_JUNGLE_WOOD,
            Items.STRIPPED_OAK_WOOD,
            Items.STRIPPED_SPRUCE_WOOD,
            Items.STRIPPED_WARPED_HYPHAE,
            Items.STRIPPED_CRIMSON_HYPHAE
    );

    private static final List<TagKey<Item>> tags = Arrays.asList(
            ItemTags.WOODEN_BUTTONS,
            ItemTags.WOODEN_PRESSURE_PLATES,
            ItemTags.WOODEN_SLABS,
            ItemTags.WOODEN_STAIRS,
            ItemTags.WOODEN_TRAPDOORS,
            ItemTags.WOODEN_DOORS,
            ItemTags.WOODEN_FENCES,
            ItemTags.BOATS,
            ItemTags.SIGNS,
            ItemTags.FENCES,
            ItemTags.LEAVES,
            ItemTags.FLOWERS,
            ItemTags.LOGS,
            ItemTags.PLANKS,
            ItemTags.WOOL_CARPETS,
            ItemTags.WOOL
    );

    private static final List<Item> similarDirt = Arrays.asList(
            Items.DIRT,
            Items.GRASS_BLOCK,
            Items.FARMLAND,
            Items.COARSE_DIRT,
            Items.PODZOL
    );

    private static final List<Item> similarFenceGate = Arrays.asList(
            Items.ACACIA_FENCE_GATE,
            Items.BIRCH_FENCE_GATE,
            Items.CRIMSON_FENCE_GATE,
            Items.OAK_FENCE_GATE,
            Items.JUNGLE_FENCE_GATE,
            Items.DARK_OAK_FENCE_GATE,
            Items.SPRUCE_FENCE_GATE,
            Items.WARPED_FENCE_GATE
    );

    private static final List<Item> similarGlass = Arrays.asList(
            Items.GLASS,
            Items.WHITE_STAINED_GLASS,
            Items.ORANGE_STAINED_GLASS,
            Items.MAGENTA_STAINED_GLASS,
            Items.LIGHT_BLUE_STAINED_GLASS,
            Items.YELLOW_STAINED_GLASS,
            Items.LIME_STAINED_GLASS,
            Items.PINK_STAINED_GLASS,
            Items.GRAY_STAINED_GLASS,
            Items.LIGHT_GRAY_STAINED_GLASS,
            Items.CYAN_STAINED_GLASS,
            Items.PURPLE_STAINED_GLASS,
            Items.BLUE_STAINED_GLASS,
            Items.BROWN_STAINED_GLASS,
            Items.GREEN_STAINED_GLASS,
            Items.RED_STAINED_GLASS,
            Items.BLACK_STAINED_GLASS
    );

    private static final List<Item> similarGlassPanes = Arrays.asList(
            Items.GLASS_PANE,
            Items.WHITE_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.MAGENTA_STAINED_GLASS_PANE,
            Items.LIGHT_BLUE_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.LIME_STAINED_GLASS_PANE,
            Items.PINK_STAINED_GLASS_PANE,
            Items.GRAY_STAINED_GLASS_PANE,
            Items.LIGHT_GRAY_STAINED_GLASS_PANE,
            Items.CYAN_STAINED_GLASS_PANE,
            Items.PURPLE_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE,
            Items.BROWN_STAINED_GLASS_PANE,
            Items.GREEN_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLACK_STAINED_GLASS_PANE
    );

    public static boolean isIgnorable(Item it) {
        final var defaultStack = it.getDefaultStack();
        return StrctureBlockData.IGNORED_ITEMS.contains(it) ||
                defaultStack.isIn(ItemTags.BEDS) ||
                defaultStack.isIn(ItemTags.DOORS) ||
                defaultStack.isIn(ItemTags.BANNERS);
    }

    public static List<Item> getSimilarItems(Item item) {
        if (strippedLogs.contains(item)) {
            return strippedLogs.stream().filter(i -> i != item).toList();
        }

        if (strippedWood.contains(item)) {
            return strippedWood.stream().filter(i -> i != item).toList();
        }

        if (similarDirt.contains(item)) {
            return similarDirt.stream().filter(i -> i != item).toList();
        }

        if (similarFenceGate.contains(item)) {
            return similarFenceGate.stream().filter(i -> i != item).toList();
        }

        if (similarGlass.contains(item)) {
            return similarGlass.stream().filter(i -> i != item).toList();
        }

        if (similarGlassPanes.contains(item)) {
            return similarGlassPanes.stream().filter(i -> i != item).toList();
        }

        return getItemTag(item)
                .map(tag ->
                        getItems(tag)
                                .stream()
                                .filter(it -> it != item)
                                .filter(it -> !strippedLogs.contains(it))
                                .filter(it -> !strippedWood.contains(it))
                                .filter(it -> !similarDirt.contains(it))
                                .filter(it -> !similarFenceGate.contains(it))
                                .filter(it -> !similarGlass.contains(it))
                                .toList()
                )
                .orElse(Collections.emptyList());
    }

    private static Optional<TagKey<Item>> getItemTag(Item item) {
        final var defaultStack = item.getDefaultStack();
        return defaultStack.streamTags().filter(tags::contains).findFirst();
    }

    public static List<Item> getItems(TagKey<Item> tag) {
        var items = new ArrayList<Item>();
        for (var it : Registries.ITEM.iterateEntries(tag)) {
            items.add(it.value());
        }
        return Collections.unmodifiableList(items);
    }

}
