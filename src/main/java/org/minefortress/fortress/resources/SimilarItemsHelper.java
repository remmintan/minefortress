package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

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
            ItemTags.PLANKS
    );

    public static List<Item> getSimilarItems(Item item) {
        if(strippedLogs.contains(item)) {
            return strippedLogs;
        }

        if(strippedWood.contains(item)) {
            return strippedWood;
        }

        return getItemTag(item)
                .map(tag -> getItems(tag).stream().filter(it -> it != item).toList())
                .orElse(Collections.emptyList());
    }

    private static Optional<TagKey<Item>> getItemTag(Item item) {
        for(var tag: tags) {
            if(contains(tag, item)) {
                return Optional.of(tag);
            }
        }
        return Optional.empty();
    }

    public static boolean contains(TagKey<Item> tag, Item item) {
        for(var it: Registry.ITEM.iterateEntries(tag)) {
            if(it.value() == item) {
                return true;
            }
        }
        return false;
    }

    private static List<Item> getItems(TagKey<Item> tag) {
        var items  = new ArrayList<Item>();
        for(var it: Registry.ITEM.iterateEntries(tag)) {
            items.add(it.value());
        }
        return Collections.unmodifiableList(items);
    }

}
