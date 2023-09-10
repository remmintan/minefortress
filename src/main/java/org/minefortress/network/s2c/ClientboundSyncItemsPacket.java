package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.apache.logging.log4j.LogManager;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressS2CPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientboundSyncItemsPacket implements FortressS2CPacket {

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
    public void handle(MinecraftClient client, FeatureSet enabledFeatures) {
        final var fortressClientManager = ((FortressMinecraftClient) client).get_FortressClientManager();
        final var resourceManager = fortressClientManager.getResourceManager();
        if(needReset) resourceManager.reset();
        for (ItemInfo info : itemInfo) {
            final var item = info.item();
            if(item == Items.STRUCTURE_VOID) continue;
            try {
                resourceManager.setItemAmount(item, info.amount(), enabledFeatures);
            } catch (IllegalArgumentException e) {
                LogManager.getLogger().warn("Failed to set item amount for item: " + item.getName().getString());
                LogManager.getLogger().warn("error: " + e.getMessage());
            }

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
