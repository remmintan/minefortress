package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.core.interfaces.networking.INetworkingReader;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreasClientManager;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.networking.registries.NetworkingReadersRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S2CSyncAreasPacket implements FortressS2CPacket {

    public static final String CHANNEL = "sync_areas";

    private final List<IAutomationAreaInfo> automationAreaInfos;

    public S2CSyncAreasPacket(List<IAutomationAreaInfo> automationAreaInfos) {
        this.automationAreaInfos = Collections.unmodifiableList(automationAreaInfos);
    }

    public S2CSyncAreasPacket(PacketByteBuf buf) {
        automationAreaInfos = new ArrayList<>();
        int size = buf.readVarInt();
        for(int i = 0; i < size; i++) {
            final INetworkingReader<IAutomationAreaInfo> reader = NetworkingReadersRegistry.findReader(IAutomationAreaInfo.class);
            automationAreaInfos.add(reader.readBuffer(buf));
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(automationAreaInfos.size());
        for(IAutomationAreaInfo info: automationAreaInfos) {
            info.writeToBuffer(buf);
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        client.execute(() -> {
            final var areasManager = getAutomationAreaManager();
            areasManager.getSavedAreasHolder().setSavedAreas(automationAreaInfos);
        });
    }

    private static IAreasClientManager getAutomationAreaManager() {
        final var provider = ClientModUtils.getManagersProvider();
        return provider.get_AreasClientManager();
    }
}
