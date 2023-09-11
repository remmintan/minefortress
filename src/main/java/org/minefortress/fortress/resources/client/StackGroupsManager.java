package org.minefortress.fortress.resources.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.util.*;
import java.util.stream.Collectors;

class StackGroupsManager {

    private final Map<ItemGroup, ClientItemStacksManager> groups = new HashMap<>();

    ClientItemStacksManager getStacksManager(ItemGroup group) {
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
        return this.getStacksManager(group).getStacks();
    }

    ItemGroup getGroup(Item item) {
        final var enabledFeatures = Optional.ofNullable(MinecraftClient.getInstance().getNetworkHandler())
                .map(ClientPlayNetworkHandler::getEnabledFeatures).orElse(null);
        return getGroup(item, enabledFeatures);
    }

    ItemGroup getGroup(Item item, FeatureSet enabledFeatures) {
        if(enabledFeatures != null) {
            final var client = MinecraftClient.getInstance();
            if(client == null) throw new IllegalStateException("Client is null");
            final var world = client.world;
            if(world != null) {
                ItemGroups.updateDisplayContext(enabledFeatures, false, world.getRegistryManager());
            }
        }
        for (ItemGroup group : ItemGroups.getGroups()) {
            if(group == Registries.ITEM_GROUP.get(ItemGroups.SEARCH)) continue;
            if(group.contains(item.getDefaultStack())) {
                return group;
            }
        }

        throw new IllegalArgumentException("Item " + item + " is not in any group");
    }

    List<FortressItemStack> getNonEmptySimilarStacks(Item item) {
        return groups.values()
                .stream()
                .flatMap(it -> it.getNonEmptySimilarStacks(item).stream())
                .toList();
    }

}
