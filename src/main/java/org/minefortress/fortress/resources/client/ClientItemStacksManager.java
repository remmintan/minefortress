package org.minefortress.fortress.resources.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ClientItemStacksManager {

    private final Map<Item, FortressItemStack> stacks = new HashMap<>();

    public FortressItemStack getStack(Item item) {
        return stacks.computeIfAbsent(item, i -> new FortressItemStack(i, 0));
    }

    public List<FortressItemStack> getNonEmptySimilarStacks(Item item) {
        return SimilarItemsHelper.getSimilarItems(item)
                .stream()
                .filter(stacks::containsKey)
                .map(stacks::get)
                .filter(it -> !it.isEmpty())
                .toList();
    }

    boolean isEmpty() {
        return stacks.values().stream().allMatch(FortressItemStack::isEmpty);
    }

    List<ItemStack> getStacks() {
        return new ArrayList<>(stacks.values().stream().filter(it -> !it.isEmpty()).toList());
    }

}
