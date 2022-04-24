package org.minefortress.fortress.resources.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class StackGroupsManager {

    private final Map<ItemGroup, ClientItemStacksManager> groups = new HashMap<>();

    ClientItemStacksManager getStcksManager(ItemGroup group) {
        return groups.computeIfAbsent(group, (g) -> new ClientItemStacksManager());
    }

    void clear() {
        groups.clear();
    }

    Set<ItemGroup> getGroups() {
        return groups.entrySet()
                .stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    List<ItemStack> getStacksFromGroup(ItemGroup group) {
        return this.getStcksManager(group).getStacks();
    }

    ItemGroup getGroup(Item item) {
        for (ItemGroup group : ItemGroup.GROUPS) {
            if(group == ItemGroup.SEARCH) continue;
            if(item.isIn(group)) {
                return group;
            }
        }

        throw new IllegalArgumentException("Item " + item + " is not in any group");
    }

}
