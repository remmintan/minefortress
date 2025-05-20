package org.minefortress.fortress.resources.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;
import net.remmintan.mods.minefortress.core.utils.ClientExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public boolean hasItems(final List<ItemInfo> costs) {
        if (ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance())) return true;

        // Create a mutable copy of available resources for simulation
        Map<Item, Integer> availableResourcesCopy = new HashMap<>();
        for (ItemGroup group : groupManager.getGroups()) { // groupManager is StackGroupsManager
            for (ItemStack stackInGroup : groupManager.getStacksFromGroup(group)) {
                availableResourcesCopy.merge(stackInGroup.getItem(), stackInGroup.getCount(), Integer::sum);
            }
        }

        for (ItemInfo cost : costs) {
            Item requiredItem = cost.item();
            int requiredAmount = cost.amount();

            // Try exact match first
            int countFromExact = availableResourcesCopy.getOrDefault(requiredItem, 0);
            if (countFromExact >= requiredAmount) {
                availableResourcesCopy.put(requiredItem, countFromExact - requiredAmount);
                continue; // Requirement met by exact item
            }

            // Use whatever exact amount is available
            int remainingNeeded = requiredAmount;
            if (countFromExact > 0) {
                availableResourcesCopy.put(requiredItem, 0);
                remainingNeeded -= countFromExact;
            }

            if (remainingNeeded == 0) continue;

            // Try similar items
            List<Item> similarItems = SimilarItemsHelper.getSimilarItems(requiredItem);
            boolean foundEnoughForThisCostItem = false;
            for (Item similarItem : similarItems) {
                int countFromSimilar = availableResourcesCopy.getOrDefault(similarItem, 0);
                if (countFromSimilar > 0) {
                    if (countFromSimilar >= remainingNeeded) {
                        availableResourcesCopy.put(similarItem, countFromSimilar - remainingNeeded);
                        remainingNeeded = 0;
                        foundEnoughForThisCostItem = true;
                        break;
                    } else {
                        availableResourcesCopy.put(similarItem, 0);
                        remainingNeeded -= countFromSimilar;
                    }
                }
            }

            if (!foundEnoughForThisCostItem && remainingNeeded > 0) {
                return false; // Cannot satisfy this cost
            }
        }
        return true; // All costs satisfied
    }

    @Override
    public int getCountIncludingSimilars(Item item) {
        if (ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance())) {
            return 999; // Or Integer.MAX_VALUE
        }
        int count = getItemAmount(item); // Gets exact item count from existing method
        for (Item similar : SimilarItemsHelper.getSimilarItems(item)) {
            count += getItemAmount(similar); // Adds counts of similar items
        }
        return count;
    }

    @Override
    public boolean hasItem(ItemInfo singleCostItem, List<ItemInfo> allCostItems) {
        // This method is tricky if allCostItems contains other items that might compete for similars.
        // A true check would be to call hasItems on a list containing just singleCostItem,
        // but that doesn't account for shared resources.
        // For display purposes, it's better to rely on the global check.
        // However, if it must provide *some* individual estimate:
        if (ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance())) return true;

        // Create a mutable copy of available resources FOR THIS CHECK ONLY
        Map<Item, Integer> availableResourcesCopy = new HashMap<>();
        for (ItemGroup group : groupManager.getGroups()) {
            for (ItemStack stackInGroup : groupManager.getStacksFromGroup(group)) {
                availableResourcesCopy.merge(stackInGroup.getItem(), stackInGroup.getCount(), Integer::sum);
            }
        }

        // Simulate satisfying all items UP TO AND INCLUDING singleCostItem from allCostItems.
        // This is to ensure that previous items in the list "consume" resources first.
        for (ItemInfo costItemFromList : allCostItems) {
            Item requiredItem = costItemFromList.item();
            int requiredAmount = costItemFromList.amount();

            int countFromExact = availableResourcesCopy.getOrDefault(requiredItem, 0);
            if (countFromExact >= requiredAmount) {
                availableResourcesCopy.put(requiredItem, countFromExact - requiredAmount);
                if (costItemFromList.equals(singleCostItem)) return true; // Current item satisfied
                continue;
            }

            int remainingNeeded = requiredAmount - countFromExact;
            if (countFromExact > 0)
                availableResourcesCopy.put(requiredItem, 0);

            if (remainingNeeded == 0 && costItemFromList.equals(singleCostItem)) return true;
            if (remainingNeeded == 0) continue;

            List<Item> similarItems = SimilarItemsHelper.getSimilarItems(requiredItem);
            boolean currentItemSatisfied = false;
            for (Item similar : similarItems) {
                int countFromSimilar = availableResourcesCopy.getOrDefault(similar, 0);
                if (countFromSimilar > 0) {
                    if (countFromSimilar >= remainingNeeded) {
                        availableResourcesCopy.put(similar, countFromSimilar - remainingNeeded);
                        remainingNeeded = 0;
                        currentItemSatisfied = true;
                        break;
                    } else {
                        availableResourcesCopy.put(similar, 0);
                        remainingNeeded -= countFromSimilar;
                    }
                }
            }

            if (costItemFromList.equals(singleCostItem)) {
                return currentItemSatisfied && remainingNeeded == 0;
            }

            if (!currentItemSatisfied && remainingNeeded > 0) {
                // A previous item in the list could not be satisfied.
                // If singleCostItem hasn't been reached yet, this means it's effectively false too
                // as resources are consumed in order.
                return false;
            }
        }
        // Should ideally not be reached if singleCostItem is in allCostItems
        return false;
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
        if (ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance())) {
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
