package org.minefortress.fortress.resources.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ClientItemStacksManager {

    private final Map<Item, FortressItemStack> stacks = new HashMap<>();

    public FortressItemStack getStack(Item item) {
        return stacks.computeIfAbsent(item, i -> new FortressItemStack(i, 0));
    }

    boolean isEmpty() {
        return stacks.values().stream().allMatch(FortressItemStack::isEmpty);
    }

    List<ItemStack> getStacks() {
        return new ArrayList<>(stacks.values().stream().filter(it -> !it.isEmpty()).toList());
    }

}
