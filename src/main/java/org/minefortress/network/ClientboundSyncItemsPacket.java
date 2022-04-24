package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientboundSyncItemsPacket implements FortressClientPacket {

    private final List<ItemInfo> itemInfo;
    private final boolean needReset;

    public ClientboundSyncItemsPacket(List<ItemInfo> itemInfo, boolean needReset) {
        this.itemInfo = Collections.unmodifiableList(itemInfo);
        this.needReset = needReset;
    }

    public ClientboundSyncItemsPacket(PacketByteBuf buf) {
        final int size = buf.readInt();
        final var tempList = new ArrayList<ItemInfo>();
        for(int i = 0; i < size; i++) {
            final int id = buf.readInt();
            final int amount = buf.readInt();
            tempList.add(new ItemInfo(Item.byRawId(id), amount));
        }

        this.itemInfo = Collections.unmodifiableList(tempList);
        this.needReset = buf.readBoolean();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var fortressClientManager = ((FortressMinecraftClient) client).getFortressClientManager();
        final var resourceManager = fortressClientManager.getResourceManager();
        if(needReset) resourceManager.reset();
        for (ItemInfo info : itemInfo) {
            resourceManager.setItemAmount(info.item(), info.amount());
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(itemInfo.size());
        for(ItemInfo itemInfo : itemInfo) {
            final var item = itemInfo.item();
            final var amount = itemInfo.amount();

            buf.writeInt(Item.getRawId(item));
            buf.writeInt(amount);
        }

        buf.writeBoolean(needReset);
    }
}
