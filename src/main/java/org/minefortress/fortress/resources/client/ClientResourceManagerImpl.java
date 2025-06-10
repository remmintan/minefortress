package org.minefortress.fortress.resources.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;
import net.remmintan.mods.minefortress.core.utils.ClientExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper;

import java.util.*;
import java.util.stream.Collectors;

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
    public Map<ItemInfo, Boolean> getMetRequirements(List<ItemInfo> costs) {
        if (ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance())) {
            return costs.stream().collect(Collectors.toMap(it -> it, it -> true, (a, b) -> b, LinkedHashMap::new));
        }

        Map<Item, Integer> availableResourcesCopy = new HashMap<>();
        for (ItemGroup group : groupManager.getGroups()) {
            for (ItemStack stackInGroup : groupManager.getStacksFromGroup(group)) {
                availableResourcesCopy.merge(stackInGroup.getItem(), stackInGroup.getCount(), Integer::sum);
            }
        }

        Map<ItemInfo, Boolean> result = new LinkedHashMap<>();

        for (ItemInfo cost : costs) {

            Item requiredItem = cost.item();
            int requiredAmount = cost.amount();

            int remainingNeeded = requiredAmount;

            int countFromExact = availableResourcesCopy.getOrDefault(requiredItem, 0);
            if (countFromExact > 0) {
                int canTake = Math.min(remainingNeeded, countFromExact);
                remainingNeeded -= canTake;
                availableResourcesCopy.put(requiredItem, countFromExact - canTake);
            }

            if (remainingNeeded > 0) {
                List<Item> similarItems = SimilarItemsHelper.getSimilarItems(requiredItem);
                for (Item similar : similarItems) {
                    if (remainingNeeded == 0) break;

                    int countFromSimilar = availableResourcesCopy.getOrDefault(similar, 0);
                    if (countFromSimilar > 0) {
                        int canTake = Math.min(remainingNeeded, countFromSimilar);
                        remainingNeeded -= canTake;
                        availableResourcesCopy.put(similar, countFromSimilar - canTake);
                    }
                }
            }

            result.put(cost, SimilarItemsHelper.isIgnorable(requiredItem) || remainingNeeded == 0);
        }

        return result;
    }

    @Override
    public boolean hasItems(final List<ItemInfo> costs) {
        return getMetRequirements(costs).values().stream().allMatch(it -> it);
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
