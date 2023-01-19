package org.minefortress.network.c2s;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.areas.AutomationAreaInfo;
import org.minefortress.network.interfaces.FortressS2CPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.minefortress.utils.ModUtils.getAreasClientManager;

public class S2CSyncAreasPacket implements FortressS2CPacket {

    public static final String CHANNEL = "sync_areas";

    private final List<AutomationAreaInfo> automationAreaInfos;

    public S2CSyncAreasPacket(List<AutomationAreaInfo> automationAreaInfos) {
        this.automationAreaInfos = Collections.unmodifiableList(automationAreaInfos);
    }

    public S2CSyncAreasPacket(PacketByteBuf buf) {
        automationAreaInfos = new ArrayList<>();
        int size = buf.readVarInt();
        for(int i = 0; i < size; i++) {
            automationAreaInfos.add(AutomationAreaInfo.readFromBuffer(buf));
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(automationAreaInfos.size());
        for(AutomationAreaInfo info: automationAreaInfos) {
            info.writeToBuffer(buf);
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        client.execute(() -> {
            final var areasManager = getAreasClientManager();
            areasManager.getSavedAreasHolder().setSavedAreas(automationAreaInfos);
        });
    }
}
