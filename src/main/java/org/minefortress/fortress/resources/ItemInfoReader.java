package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.INetworkingReader;

public class ItemInfoReader implements INetworkingReader<ItemInfo> {
    @Override
    public ItemInfo readBuffer(PacketByteBuf buf) {
        final int id = buf.readInt();
        final int amount = buf.readInt();
        return new ItemInfo(Item.byRawId(id), amount);
    }

    @Override
    public boolean canReadForType(Class<?> type) {
        return type.isAssignableFrom(ItemInfo.class);
    }
}
