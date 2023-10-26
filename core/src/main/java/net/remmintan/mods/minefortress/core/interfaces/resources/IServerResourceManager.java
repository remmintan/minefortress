package net.remmintan.mods.minefortress.core.interfaces.resources;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

public interface IServerResourceManager extends IResourceManager {
    void syncAll();

    IItemInfo createItemInfo(Item item, int amount);
    void setItemAmount(Item item, int amount);
    void increaseItemAmount(Item item, int amount);
    void reserveItems(UUID uuid, List<IItemInfo> stacks);
    void removeReservedItem(UUID taskId, Item item);
    void removeItemIfExists(UUID taskId, Item item);
    void removeItems(List<IItemInfo> items);
    void returnReservedItems(UUID taskId);

    void write(NbtCompound tag);
    void read(NbtCompound tag);
    void tick(ServerPlayerEntity player);

    List<ItemStack> getAllItems();

}
