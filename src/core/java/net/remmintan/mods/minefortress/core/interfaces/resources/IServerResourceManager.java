package net.remmintan.mods.minefortress.core.interfaces.resources;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ISyncableServerManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IServerResourceManager extends IResourceManager, IServerManager, ISyncableServerManager {
    ItemInfo createItemInfo(Item item, int amount);
    void setItemAmount(Item item, int amount);
    void increaseItemAmount(Item item, int amount);

    void reserveItems(UUID uuid, List<ItemInfo> stacks);
    void removeReservedItem(UUID taskId, Item item);
    void removeItemIfExists(UUID taskId, Item item);

    void removeItems(List<ItemInfo> items);
    void returnReservedItems(UUID taskId);

    List<ItemStack> getAllItems();

    boolean hasEatableItem();

    @NotNull
    Optional<ItemStack> getEatableItem();

}
