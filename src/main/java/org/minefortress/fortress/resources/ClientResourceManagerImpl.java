package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.*;

public class ClientResourceManagerImpl implements ClientResourceManager {

    private final Map<ItemGroup, Map<Item, ItemStack>> resources = new HashMap<>();

    @Override
    public Set<ItemGroup> getGroups() {
        return resources.keySet();
    }

    @Override
    public boolean hasStacks(List<ItemStack> stacks) {
        return stacks
                .stream()
                .map(this::getInfo)
                .allMatch(it -> {
                    final var item = it.item();
                    final var group = getGroup(item);
                    final var itemStack = resources.get(group).get(item);
                    if(itemStack == null) return false;
                    return itemStack.getCount() >= it.amount();
                });
    }

    private ItemInfo getInfo(ItemStack stack) {
        return new ItemInfo(stack.getItem(), stack.getCount());
    }

    @Override
    public List<ItemStack> getStacks(ItemGroup group) {
        return new ArrayList<>(getStacksForGroup(group).values());
    }

    @Override
    public void setItemAmount(Item item, int amount) {
        final var group = this.getGroup(item);
        final var stacksForGroup = getStacksForGroup(group);
        if(amount > 0) {
            stacksForGroup.put(item, new ItemStack(item, amount));
        } else {
            stacksForGroup.remove(item);
        }

        if(stacksForGroup.isEmpty()) {
            resources.remove(group);
        }
    }

    private ItemGroup getGroup(Item item) {
        for (ItemGroup group : ItemGroup.GROUPS) {
            if(item.isIn(group)) {
                return group;
            }
        }

        throw new IllegalArgumentException("Item " + item + " is not in any group");
    }

    private Map<Item, ItemStack> getStacksForGroup(ItemGroup group) {
        return resources.computeIfAbsent(group, k -> new HashMap<>());
    }

}
