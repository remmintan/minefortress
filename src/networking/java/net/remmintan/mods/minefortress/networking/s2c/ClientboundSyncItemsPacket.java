package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.networking.registries.NetworkingReadersRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientboundSyncItemsPacket implements FortressS2CPacket {

    private final List<ItemInfo> itemInfos;
    private final boolean needReset;

    public ClientboundSyncItemsPacket(List<ItemInfo> itemInfos, boolean needReset) {
        this.itemInfos = Collections.unmodifiableList(itemInfos);
        this.needReset = needReset;
    }

    public ClientboundSyncItemsPacket(PacketByteBuf buf) {
        final int size = buf.readInt();
        final var tempList = new ArrayList<ItemInfo>();
        final var reader = NetworkingReadersRegistry.findReader(ItemInfo.class);
        for(int i = 0; i < size; i++) {
            tempList.add(reader.readBuffer(buf));
        }

        this.itemInfos = Collections.unmodifiableList(tempList);
        this.needReset = buf.readBoolean();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var provider = getManagersProvider();
        final var resourceManager = provider.get_ClientFortressManager().getResourceManager();
        resourceManager.sync(itemInfos, needReset);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(itemInfos.size());
        for (ItemInfo itemInfo : itemInfos) {
            final var item = itemInfo.item();
            final var amount = itemInfo.amount();

            buf.writeInt(Item.getRawId(item));
            buf.writeInt(amount);
        }

        buf.writeBoolean(needReset);
    }
}
