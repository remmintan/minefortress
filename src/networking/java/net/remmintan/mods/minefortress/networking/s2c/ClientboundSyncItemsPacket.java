package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;
import net.remmintan.mods.minefortress.networking.registries.NetworkingReadersRegistry;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientboundSyncItemsPacket implements FortressS2CPacket {

    private final List<IItemInfo> itemInfo;
    private final boolean needReset;

    public ClientboundSyncItemsPacket(List<IItemInfo> itemInfo, boolean needReset) {
        this.itemInfo = Collections.unmodifiableList(itemInfo);
        this.needReset = needReset;
    }

    public ClientboundSyncItemsPacket(PacketByteBuf buf) {
        final int size = buf.readInt();
        final var tempList = new ArrayList<IItemInfo>();
        final var reader = NetworkingReadersRegistry.findReader(IItemInfo.class);
        for(int i = 0; i < size; i++) {
            final int id = buf.readInt();
            final int amount = buf.readInt();
            tempList.add(reader.readBuffer(buf));
        }

        this.itemInfo = Collections.unmodifiableList(tempList);
        this.needReset = buf.readBoolean();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var provider = getManagersProvider();
        final var resourceManager = provider.get_ClientFortressManager().getResourceManager();
        if(needReset) resourceManager.reset();
        for (IItemInfo info : itemInfo) {
            final var item = info.item();
            if(item == Items.STRUCTURE_VOID) continue;
            try {
                resourceManager.setItemAmount(item, info.amount());
            } catch (IllegalArgumentException e) {
                LogManager.getLogger().warn("Failed to set item amount for item: " + item.getName().getString());
                LogManager.getLogger().warn("error: " + e.getMessage());
            }
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(itemInfo.size());
        for(IItemInfo itemInfo : itemInfo) {
            final var item = itemInfo.item();
            final var amount = itemInfo.amount();

            buf.writeInt(Item.getRawId(item));
            buf.writeInt(amount);
        }

        buf.writeBoolean(needReset);
    }
}
