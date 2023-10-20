package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.INetworkingReader;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;

public class ItemInfoReader implements INetworkingReader<IItemInfo> {
    @Override
    public IItemInfo readBuffer(PacketByteBuf buf) {
        final int id = buf.readInt();
        final int amount = buf.readInt();
        return new ItemInfo(Item.byRawId(id), amount);
    }

    @Override
    public boolean canReadForType(Class<?> type) {
        return type.isAssignableFrom(IItemInfo.class);
    }
}
