package org.minefortress.fortress.resources.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.SimilarItemsHelper;
import org.minefortress.utils.ModUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientResourceManagerImpl implements IClientResourceManager {

    private final StackGroupsManager groupManager = new StackGroupsManager();

    @Override
    public Set<ItemGroup> getGroups() {
        return groupManager.getGroups();
    }

    @Override
    public boolean hasStacks(List<ItemStack> stacks) {
        final var itemInfos = stacks
                .stream()
                .map(it -> new ItemInfo(it.getItem(), it.getCount()))
                .toList();
        return hasItems(itemInfos);
    }

    @Override
    public boolean hasItems(final List<IItemInfo> stacks) {
        if(ModUtils.getFortressClientManager().isCreative()) return true;
        return stacks
                .stream()
                .allMatch(it -> {
                    final var item = it.item();
                    final var amount = it.amount();
                    return this.hasItem(item, amount, stacks);
                });
    }

    @Override
    public boolean hasItem(IItemInfo itemInfo, List<IItemInfo> items) {
        final var item = itemInfo.item();
        final var amount = itemInfo.amount();
        return hasItem(item, amount, items);
    }

    private boolean hasItem(Item item, int amount, List<IItemInfo> items) {
        final var group = groupManager.getGroup(item);
        final var manager = groupManager.getStacksManager(group);
        final var stack = manager.getStack(item);
        if (stack == null) return false;
        final var availableAmount = stack.getCount();
        if(availableAmount >= amount) return true;
        final var amountOfNonEmptySimilarElements = groupManager
                .getNonEmptySimilarStacks(item)
                .stream()
                .map(ItemStack::getCount)
                .reduce(0, Integer::sum);

        final var similarItemsSet = new HashSet<>(SimilarItemsHelper.getSimilarItems(item));
        final var requiredSimilarItems = items.stream()
                .filter(it -> similarItemsSet.contains(it.item()))
                .mapToInt(ItemInfo::amount)
                .sum();

        return (amountOfNonEmptySimilarElements - requiredSimilarItems + availableAmount) >= amount;
    }

    @Override
    public List<ItemStack> getStacks(ItemGroup group) {
        return groupManager.getStacksFromGroup(group);
    }

    @Override
    public void setItemAmount(Item item, int amount) {
        final var group = groupManager.getGroup(item);
        final var manager = groupManager.getStacksManager(group);
        manager.getStack(item).setCount(amount);
    }

    @Override
    public int getItemAmount(Item item) {
        if(ModUtils.getFortressClientManager().isCreative()) {
            return 999;
        }
        final var group = groupManager.getGroup(item);
        final var manager = groupManager.getStacksManager(group);
        return manager.getStack(item).getCount();
    }

    @Override
    public void reset() {
        groupManager.clear();
    }

}
