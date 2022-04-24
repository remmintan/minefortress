package org.minefortress.fortress.resources.server;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.List;
import java.util.UUID;

public interface ServerResourceManager {

    void addItem(Item item, int amount);
    void reserveItems(UUID uuid, List<ItemInfo> stacks);
    void removeReservedItem(UUID taskId, Item item);
    void returnReservedItems(UUID taskId);

    void write(NbtCompound tag);
    void read(NbtCompound tag);
    void tick(ServerPlayerEntity player);

}
