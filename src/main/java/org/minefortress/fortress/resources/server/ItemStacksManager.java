package org.minefortress.fortress.resources.server;

import net.minecraft.item.Item;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.SimilarItemsHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ItemStacksManager {

    private final Map<Item, EasyItemStack> stacks = new HashMap<>();

    public void clear() {
        stacks.clear();
    }

    public EasyItemStack getStack(Item item) {
        return stacks.computeIfAbsent(item, EasyItemStack::new);
    }

    public List<EasyItemStack> getNonEmptySimilarStacks(Item item) {
        return SimilarItemsHelper.getSimilarItems(item)
                .stream()
                .filter(stacks::containsKey)
                .map(stacks::get)
                .filter(it -> it.getAmount() > 0)
                .toList();
    }

    public List<ItemInfo> getAll() {
        return stacks
                .entrySet()
                .stream()
                .map(e -> new ItemInfo(e.getKey(), e.getValue().getAmount()))
                .collect(Collectors.toList());
    }

}
