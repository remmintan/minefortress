package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import org.minefortress.blueprints.data.BlueprintBlockData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    private static final List<Tag.Identified<Item>> tags = Arrays.asList(
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
            ItemTags.PLANKS
    );

    private static final List<Item> similarDirt = Arrays.asList(
            Items.DIRT,
            Items.GRASS_BLOCK,
            Items.FARMLAND,
            Items.COARSE_DIRT,
            Items.PODZOL
    );

    public static boolean isIgnorable(Item it) {
        return BlueprintBlockData.IGNORED_ITEMS.contains(it) || ItemTags.BEDS.contains(it) || ItemTags.DOORS.contains(it);
    }

    public static List<Item> getSimilarItems(Item item) {
        if(strippedLogs.contains(item)) {
            return strippedLogs.stream().filter(i -> i != item).toList();
        }

        if(strippedWood.contains(item)) {
            return strippedWood.stream().filter(i -> i != item).toList();
        }

        if(similarDirt.contains(item)) {
            return similarDirt.stream().filter(i -> i != item).toList();
        }

        return getItemTag(item)
                .map(tag ->
                        tag.values()
                                .stream()
                                .filter(it -> it != item)
                                .filter(it -> !strippedLogs.contains(it))
                                .filter(it -> !strippedWood.contains(it))
                                .filter(it -> !similarDirt.contains(it))
                                .toList()
                )
                .orElse(Collections.emptyList());
    }

    private static Optional<Tag.Identified<Item>> getItemTag(Item item) {
        for(var tag: tags) {
            if(tag.contains(item)) {
                return Optional.of(tag);
            }
        }
        return Optional.empty();
    }

}
