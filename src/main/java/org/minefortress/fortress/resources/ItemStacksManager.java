package org.minefortress.fortress.resources;

import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemStacksManager {

    private final Map<Item, EasyItemStack> stacks = new HashMap<>();

    public void clear() {
        stacks.clear();
    }

    public EasyItemStack getStack(Item item) {
        return stacks.computeIfAbsent(item, (i) -> new EasyItemStack());
    }

    public List<ItemInfo> getAll() {
        return stacks
                .entrySet()
                .stream()
                .map(e -> new ItemInfo(e.getKey(), e.getValue().getAmount()))
                .collect(Collectors.toList());
    }

}
