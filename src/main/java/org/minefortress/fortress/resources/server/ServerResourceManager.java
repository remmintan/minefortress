package org.minefortress.fortress.resources.server;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.resources.FortressResourceManager;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.List;
import java.util.UUID;

public interface ServerResourceManager extends FortressResourceManager {

    void syncAll();
    void setItemAmount(Item item, int amount);
    void increaseItemAmount(Item item, int amount);
    void reserveItems(UUID uuid, List<ItemInfo> stacks);
    void removeReservedItem(UUID taskId, Item item);
    void removeItemIfExists(UUID taskId, Item item);
    void removeItems(List<ItemInfo> items);
    void returnReservedItems(UUID taskId);

    void write(NbtCompound tag);
    void read(NbtCompound tag);
    void tick(ServerPlayerEntity player);

    List<ItemStack> getAllItems();

}
