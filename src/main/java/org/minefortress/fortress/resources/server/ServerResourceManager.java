package org.minefortress.fortress.resources.server;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundSyncItemsPacket;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fortress.resources.client.FortressItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ServerResourceManager implements IServerResourceManager, ITickableManager, IWritableManager {

    private final Synchronizer synchronizer = new Synchronizer();

    private final ItemStacksManager resources = new ItemStacksManager();
    private final Map<UUID, ItemStacksManager> reservedResources = new HashMap<>();
    private final MinecraftServer server;

    public ServerResourceManager(MinecraftServer server) {
        this.server = server;

        final var reader = new ServerStartingInventoryReader(server);
        final var inventoryStartingSlots = reader.readStartingSlots();
        for (var slot : inventoryStartingSlots) {
            resources.getStack(slot.item()).increaseBy(slot.amount());
        }
    }


    public ItemInfo createItemInfo(Item item, int amount) {
        return new ItemInfo(item, amount);
    }

    @Override
    public void setItemAmount(Item item, int amount) {
        final var stack = resources.getStack(item);
        stack.setAmount(amount);

        synchronizer.syncItem(item, stack.getAmount());
    }

    @Override
    public void increaseItemAmount(Item item, int amount) {
        final var stack = resources.getStack(item);
        stack.increaseBy(amount);

        synchronizer.syncItem(item, stack.getAmount());
    }

    @Override
    public void reserveItems(UUID taskId, List<ItemInfo> infos) {
        if (!hasItems(infos)) {
            String availableResStr = resources.getAll().stream().map(it -> it.item().toString() + ":" + it.amount()).collect(Collectors.joining(", "));
            String requiredResStr = infos.stream().map(it -> it.item().toString() + ":" + it.amount()).collect(Collectors.joining(", "));
            LogManager.getLogger().error("ResourceManager: Not enough resources to reserve. TaskId: " + taskId +
                    ", Required: [" + requiredResStr + "], Available: [" + availableResStr + "]");
            throw new IllegalStateException("Not enough resources to reserve items for task " + taskId);
        }

        final var reservedItemsManager = this.getManagerFromTaskId(taskId);
        final var infosToSync = new ArrayList<ItemInfo>();

        for (ItemInfo costInfo : infos) {
            Item requiredItem = costInfo.item();
            int amountToReserve = costInfo.amount();

            // Try to take from exact item first
            EasyItemStack exactResourceStack = resources.getStack(requiredItem);
            int takenFromExact = Math.min(amountToReserve, exactResourceStack.getAmount());

            if (takenFromExact > 0) {
                exactResourceStack.decreaseBy(takenFromExact);
                reservedItemsManager.getStack(requiredItem).increaseBy(takenFromExact);
                infosToSync.add(new ItemInfo(requiredItem, exactResourceStack.getAmount()));
                amountToReserve -= takenFromExact;
            }

            if (amountToReserve > 0) {
                // Need to take from similar items
                List<Item> similarItems = SimilarItemsHelper.getSimilarItems(requiredItem);
                for (Item similarItem : similarItems) {
                    if (amountToReserve == 0) break;

                    EasyItemStack similarResourceStack = resources.getStack(similarItem);
                    int takenFromSimilar = Math.min(amountToReserve, similarResourceStack.getAmount());

                    if (takenFromSimilar > 0) {
                        similarResourceStack.decreaseBy(takenFromSimilar);
                        reservedItemsManager.getStack(similarItem).increaseBy(takenFromSimilar);
                        infosToSync.add(new ItemInfo(similarItem, similarResourceStack.getAmount()));
                        amountToReserve -= takenFromSimilar;
                    }
                }
            }

            if (amountToReserve > 0) {
                // This should not be reached if hasItems is correct and there are no concurrency issues.
                LogManager.getLogger().error("ResourceManager: Error during reservation. Could not fully reserve " +
                        requiredItem + " (needed " + costInfo.amount() + ", still need " + amountToReserve +
                        " after trying exact and similars). TaskId: " + taskId);
                // Potentially throw an exception or handle this state if partial reservation is not allowed.
            }
        }

        synchronizer.syncAll(infosToSync);
    }

    @Override
    public void removeReservedItem(UUID taskId, Item itemUsedInConcept) {
        removeReservedItemInternal(taskId, itemUsedInConcept, false);
    }

    @Override
    public void removeItemIfExists(UUID taskId, Item itemUsedInConcept) {
        removeReservedItemInternal(taskId, itemUsedInConcept, true);
    }

    private void removeReservedItemInternal(UUID taskId, Item itemUsedInConcept, boolean ignoreIfNotEnough) {
        if (!reservedResources.containsKey(taskId)) {
            if (!ignoreIfNotEnough) {
                LogManager.getLogger().warn("Task ID " + taskId + " not found in reserved resources for item " + itemUsedInConcept);
            }
            return;
        }

        final var taskReservedItems = reservedResources.get(taskId); // ItemStacksManager for this task's reservations

        // Try to consume the exact item if it was specifically reserved under its own type
        EasyItemStack specificReservedStack = taskReservedItems.getStack(itemUsedInConcept);
        if (specificReservedStack.getAmount() > 0) {
            specificReservedStack.decrease();
            return;
        }

        // If the exact item (itemUsedInConcept) wasn't found or is depleted in the reservation,
        // it implies a similar item might have been substituted during reservation.
        // We iterate through items similar to itemUsedInConcept to find what might have been reserved.
        List<Item> potentialSubstitutes = SimilarItemsHelper.getSimilarItems(itemUsedInConcept);
        for (Item substituteItem : potentialSubstitutes) {
            EasyItemStack substituteReservedStack = taskReservedItems.getStack(substituteItem);
            if (substituteReservedStack.getAmount() > 0) {
                substituteReservedStack.decrease();
                return; // Consumed a similar item that was in the reservation
            }
        }

        if (!ignoreIfNotEnough) {
            LogManager.getLogger().warn("Could not fulfill consumption of conceptual item " + itemUsedInConcept +
                    " from task " + taskId + " reservation. Neither exact nor a suitable similar item found in the task's reservation.");
        }
    }

    @Override
    public void removeItems(List<ItemInfo> itemsToRemove) {
        if (ServerExtensionsKt.isCreativeFortress(server)) return; // No need to remove in creative

        final var infosToSync = new ArrayList<ItemInfo>();

        for (ItemInfo itemInfoToRemove : itemsToRemove) {
            Item conceptualItem = itemInfoToRemove.item();
            int amountStillNeededToRemove = itemInfoToRemove.amount();

            if (amountStillNeededToRemove <= 0) continue;

            // Try to remove from the exact item first
            final EasyItemStack exactItemStack = resources.getStack(conceptualItem);
            int amountInExactStack = exactItemStack.getAmount();
            if (amountInExactStack > 0) {
                int amountToRemoveFromExact = Math.min(amountStillNeededToRemove, amountInExactStack);
                exactItemStack.decreaseBy(amountToRemoveFromExact);
                amountStillNeededToRemove -= amountToRemoveFromExact;
                infosToSync.add(new ItemInfo(conceptualItem, exactItemStack.getAmount()));
            }

            if (amountStillNeededToRemove > 0) {
                // Still need to remove more, try similar items
                List<Item> similarItems = SimilarItemsHelper.getSimilarItems(conceptualItem);
                for (Item similarItem : similarItems) {
                    if (amountStillNeededToRemove == 0) break;

                    final EasyItemStack similarItemStack = resources.getStack(similarItem);
                    int amountInSimilarStack = similarItemStack.getAmount();

                    if (amountInSimilarStack > 0) {
                        int amountToRemoveFromSimilar = Math.min(amountStillNeededToRemove, amountInSimilarStack);
                        similarItemStack.decreaseBy(amountToRemoveFromSimilar);
                        amountStillNeededToRemove -= amountToRemoveFromSimilar;
                        infosToSync.add(new ItemInfo(similarItem, similarItemStack.getAmount()));
                    }
                }
            }

            if (amountStillNeededToRemove > 0) {
                // This case should ideally not be reached if hasItems was checked properly before calling removeItems
                // Or if the game logic allows for partial removal.
                LogManager.getLogger().warn(
                        String.format("Could not remove full amount of %s. Still needed: %d. This might indicate an issue with pre-checks.",
                                conceptualItem.getName().getString(),
                                amountStillNeededToRemove
                        )
                );
            }
        }

        if (!infosToSync.isEmpty()) {
            synchronizer.syncAll(infosToSync);
        }
    }

    private static boolean isEatable(ItemStack stack) {
        if (stack.isEmpty() || !stack.getItem().isFood())
            return false;

        final var foodComponent = stack.getItem().getFoodComponent();
        final var statusEffects = foodComponent.getStatusEffects();
        if (statusEffects.isEmpty())
            return true;

        for (Pair<StatusEffectInstance, Float> statusEffect : statusEffects) {
            if (statusEffect.getFirst().getEffectType().getCategory() == StatusEffectCategory.HARMFUL)
                return false;
        }

        return true;
    }

    @Override
    public void returnReservedItems(UUID taskId) {
        if (!reservedResources.containsKey(taskId)) return;
        final var manager = this.getManagerFromTaskId(taskId);

        final var infosToSync = new ArrayList<ItemInfo>();
        for (ItemInfo info : manager.getAll()) {
            final var item = info.item();
            final var stack = this.resources.getStack(item);
            final var amount = info.amount();
            stack.increaseBy(amount);

            final var infoToSync = new ItemInfo(item, stack.getAmount());
            infosToSync.add(infoToSync);
        }

        synchronizer.syncAll(infosToSync);
        this.reservedResources.remove(taskId);
    }

    @Override
    public void write(NbtCompound tag) {
        final var stacks = new NbtList();
        for (ItemInfo info : resources.getAll()) {
            final var item = info.item();
            final var amount = info.amount();

            final var stack = new NbtCompound();
            final var rawId = Item.getRawId(item);
            stack.putInt("id", rawId);
            stack.putInt("amount", amount);

            stacks.add(stack);
        }

        tag.put("resources", stacks);
    }

    @Override
    public void tick(@NotNull MinecraftServer server, @NotNull ServerWorld world, @Nullable ServerPlayerEntity player) {
        synchronizer.sync(player);
    }

    @Override
    public void read(NbtCompound tag) {
        if (tag.contains("resources")) {
            this.resources.clear();
            final var resourcesTags = tag.getList("resources", NbtList.COMPOUND_TYPE);
            final var size = resourcesTags.size();
            for (int i = 0; i < size; i++) {
                final var resourceTag = resourcesTags.getCompound(i);
                final var id = resourceTag.getInt("id");
                final var amount = resourceTag.getInt("amount");
                final var item = Item.byRawId(id);

                this.resources.getStack(item).increaseBy(amount);
            }
        }
    }

    @Override
    public List<ItemStack> getAllItems() {
        return resources.getAll().stream()
                .filter(info -> info.amount() > 0)
                .map(it -> (ItemStack) new FortressItemStack(it.item(), it.amount()))
                .toList();
    }

    @Override
    public boolean hasEatableItem() {
        return getEatableItem().isPresent();
    }

    @Override
    @NotNull
    public Optional<ItemStack> getEatableItem() {
        return this
                .getAllItems()
                .stream()
                .filter(ServerResourceManager::isEatable)
                .max(
                        Comparator.comparingDouble(stack ->
                                {
                                    final var foodComponent = stack.getItem().getFoodComponent();
                                    //noinspection DataFlowIssue
                                    return foodComponent.getHunger() * foodComponent.getSaturationModifier() * 2.0f;
                                }
                        )
                );
    }

    @Override
    public boolean hasItems(List<ItemInfo> infos) {
        if (ServerExtensionsKt.isCreativeFortress(server)) return true;

        // Create a mutable copy of available resources for simulation
        Map<Item, Integer> availableResourcesCopy = new HashMap<>();
        for (ItemInfo currentItemInfo : resources.getAll()) { // resources is ItemStacksManager
            availableResourcesCopy.put(currentItemInfo.item(), currentItemInfo.amount());
        }

        for (ItemInfo cost : infos) {
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

    private ItemStacksManager getManagerFromTaskId(UUID taskId) {
        return reservedResources.computeIfAbsent(taskId, k -> new ItemStacksManager());
    }

    public void sync() {
        this.synchronizer.reset();
        this.synchronizer.syncAll(resources.getAll());
    }

    private static class Synchronizer {

        private final List<ItemInfo> infosToSync = new ArrayList<>();
        private boolean needReset = false;

        void reset() {
            this.infosToSync.clear();
            this.needReset = true;
        }

        void sync(ServerPlayerEntity player) {
            if (player == null || (infosToSync.isEmpty() && !needReset)) return;
            final var packet = new ClientboundSyncItemsPacket(infosToSync, needReset);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_RESOURCES_SYNC, packet);
            infosToSync.clear();
            this.needReset = false;
        }

        void syncItem(Item item, int amount) {
            infosToSync.add(new ItemInfo(item, amount));
        }

        void syncAll(List<ItemInfo> items) {
            infosToSync.addAll(items);
        }

    }

}
