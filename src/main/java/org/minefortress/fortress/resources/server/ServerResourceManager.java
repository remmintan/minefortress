package org.minefortress.fortress.resources.server;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundSyncItemsPacket;
import org.apache.logging.log4j.LogManager;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.SimilarItemsHelper;
import org.minefortress.fortress.resources.client.FortressItemStack;

import java.util.*;

public class ServerResourceManager implements IServerResourceManager {

    private final Synchronizer synchronizer = new Synchronizer();

    private final ItemStacksManager resources = new ItemStacksManager();
    private final Map<UUID, ItemStacksManager> reservedResources = new HashMap<>();

    public ServerResourceManager(MinecraftServer server) {
        final var reader = new ServerStartingInventoryReader(server);
        final var inventoryStartingSlots = reader.readStartingSlots();
        for(var slot : inventoryStartingSlots) {
            resources.getStack(slot.item()).increaseBy(slot.amount());
        }
    }

    public IItemInfo createItemInfo(Item item, int amount) {
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
    public void reserveItems(UUID taskId, List<IItemInfo> infos) {
        if(!hasItems(infos)) throw new IllegalStateException("Not enough resources");

        final var reservedItemsManager = this.getManagerFromTaskId(taskId);
        final var infosToSync = new ArrayList<ItemInfo>();
        for(IItemInfo info : infos) {
            final var item = info.item();
            final var requiredAmount = info.amount();
            final var stack = resources.getStack(item);
            final var reservedStack = reservedItemsManager.getStack(item);

            var yetToFulfill = requiredAmount - stack.getAmount();

            if(yetToFulfill>0) {
                final var existingAmount = stack.getAmount();
                stack.decreaseBy(existingAmount);
                reservedStack.increaseBy(existingAmount);
            } else {
                stack.decreaseBy(requiredAmount);
                reservedStack.increaseBy(requiredAmount);
            }
            infosToSync.add(new ItemInfo(item, stack.getAmount()));

            if(yetToFulfill>0) {
                final var similarItems = resources.getNonEmptySimilarStacks(item);
                for(var similarStack : similarItems) {
                    var newReservedStack = reservedItemsManager.getStack(similarStack.getItem());
                    final var similarStackAmount = similarStack.getAmount();
                    if(similarStackAmount >=yetToFulfill) {
                        similarStack.decreaseBy(yetToFulfill);
                        newReservedStack.increaseBy(yetToFulfill);
                        yetToFulfill = 0;
                    } else {
                        yetToFulfill -= similarStackAmount;
                        similarStack.decreaseBy(similarStackAmount);
                        newReservedStack.increaseBy(similarStackAmount);
                    }
                    infosToSync.add(new ItemInfo(similarStack.getItem(), similarStack.getAmount()));
                    if(yetToFulfill==0) break;
                }
            }
        }

        synchronizer.syncAll(infosToSync);
    }

    @Override
    public void removeReservedItem(UUID taskId, Item item) {
        boolean ignoreWhenNotEnough = false;

        removeReservedItem(taskId, item, ignoreWhenNotEnough);
    }

    private void removeReservedItem(UUID taskId, Item item, boolean ignoreWhenNotEnough) {
        if(!(item instanceof BlockItem)) return;

        final var reservedItemsManager = this.getManagerFromTaskId(taskId);
        final var reservedStack = reservedItemsManager.getStack(item);
        if(reservedStack.getAmount() >= 1) {
            reservedStack.decrease();
        } else {
            final var nonEmptySimilarStacks = reservedItemsManager.getNonEmptySimilarStacks(item);
            if(!nonEmptySimilarStacks.isEmpty()) {
                final var similarStack = nonEmptySimilarStacks.get(0);
                similarStack.decrease();
            } else{
                if(!ignoreWhenNotEnough) {
                    LogManager.getLogger().warn("Tried to remove reserved item, but not enough items: " + item.getName().getContent());
                }
            }
        }
    }

    @Override
    public void removeItemIfExists(UUID taskId, Item item) {
        removeReservedItem(taskId, item, true);
    }

    public void removeItems(List<IItemInfo> items) {
        for(IItemInfo itemInfo:items) {
            final var stack = resources.getStack(itemInfo.item());
            if(stack.getAmount()<=0)return;
            stack.decreaseBy(itemInfo.amount());
            synchronizer.syncItem(itemInfo.item(), stack.getAmount());
        }
    }

    @Override
    public void returnReservedItems(UUID taskId) {
        if(!reservedResources.containsKey(taskId)) return;
        final var manager = this.getManagerFromTaskId(taskId);

        final var infosToSync = new ArrayList<ItemInfo>();
        for(ItemInfo info: manager.getAll()) {
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
        for(ItemInfo info : resources.getAll()) {
            final var item = info.item();
            final var amount = info.amount();

            final var stack = new NbtCompound();
            final var rawId = Item.getRawId(item);
            stack.putInt("id", rawId);
            stack.putInt("amount", amount);

            stacks.add(stack);
        }

        tag.put("resources", stacks);
//        this.resources.clear();
    }

    @Override
    public void read(NbtCompound tag) {
        if(tag.contains("resources")) {
            this.resources.clear();
            final var resourcesTags = tag.getList("resources", NbtList.COMPOUND_TYPE);
            final var size = resourcesTags.size();
            for(int i = 0; i < size; i++) {
                final var resourceTag = resourcesTags.getCompound(i);
                final var id = resourceTag.getInt("id");
                final var amount = resourceTag.getInt("amount");
                final var item = Item.byRawId(id);

                this.resources.getStack(item).increaseBy(amount);
            }
        }
    }

    @Override
    public void tick(ServerPlayerEntity player) {
        synchronizer.sync(player);
    }

    @Override
    public List<ItemStack> getAllItems() {
        return resources.getAll().stream()
                .filter(info -> info.amount() > 0)
                .map(it -> (ItemStack)new FortressItemStack(it.item(), it.amount()))
                .toList();
    }

    @Override
    public boolean hasItems(List<IItemInfo> infos) {
        for(IItemInfo info : infos) {
            final var item = info.item();
            if(item == Items.FLINT_AND_STEEL || item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET) continue;
            final var amount = info.amount();
            final var stack = resources.getStack(item);
            if(!stack.hasEnough(amount)) {
                final var sumAmountOfSimilarItems = resources.getNonEmptySimilarStacks(item)
                        .stream()
                        .map(EasyItemStack::getAmount)
                        .reduce(0, Integer::sum);

                final var similarItemsSet = new HashSet<>(SimilarItemsHelper.getSimilarItems(item));
                final var requiredSimilarItems = infos.stream()
                        .filter(it -> similarItemsSet.contains(it.item()))
                        .mapToInt(IItemInfo::amount)
                        .sum();

                if(sumAmountOfSimilarItems - requiredSimilarItems + stack.getAmount() < amount) return false;
            }
        }
        return true;
    }
    
    private ItemStacksManager getManagerFromTaskId(UUID taskId) {
        return reservedResources.computeIfAbsent(taskId, k -> new ItemStacksManager());
    }

    public void syncAll() {
        this.synchronizer.reset();
        this.synchronizer.syncAll(resources.getAll());
    }

    private static class Synchronizer {

        private final List<IItemInfo> infosToSync = new ArrayList<>();
        private boolean needReset = false;

        void reset() {
            this.infosToSync.clear();
            this.needReset = true;
        }

        void sync(ServerPlayerEntity player) {
            if(player == null || (infosToSync.isEmpty() && !needReset)) return;
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
