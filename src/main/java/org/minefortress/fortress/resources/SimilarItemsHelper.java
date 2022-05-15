package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SimilarItemsHelper {

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
            ItemTags.FLOWERS
    );

    public static List<Item> getSimilarItems(Item item) {
        return getItemTag(item)
                .map(tag -> tag.values().stream().filter(it -> it != item).toList())
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
