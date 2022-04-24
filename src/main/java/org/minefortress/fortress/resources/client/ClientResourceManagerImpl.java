package org.minefortress.fortress.resources.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.*;

public class ClientResourceManagerImpl implements ClientResourceManager {

    private final StackGroupsManager groupManager = new StackGroupsManager();

    @Override
    public Set<ItemGroup> getGroups() {
        return groupManager.getGroups();
    }

    @Override
    public boolean hasStacks(List<ItemStack> stacks) {
        return stacks
                .stream()
                .allMatch(it -> {
                    final var item = it.getItem();
                    final var group = groupManager.getGroup(item);
                    final var itemStack = groupManager.getStcksManager(group).getStack(item);
                    if(itemStack == null) return false;
                    return itemStack.getCount() >= it.getCount();
                });
    }

    @Override
    public List<ItemStack> getStacks(ItemGroup group) {
        return groupManager.getStacksFromGroup(group);
    }

    @Override
    public void setItemAmount(Item item, int amount) {
        final var group = groupManager.getGroup(item);
        final var manager = groupManager.getStcksManager(group);
        manager.getStack(item).setCount(amount);
    }

    @Override
    public void reset() {
        groupManager.clear();
    }

}
