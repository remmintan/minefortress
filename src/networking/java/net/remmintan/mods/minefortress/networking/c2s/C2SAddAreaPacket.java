package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.networking.registries.NetworkingReadersRegistry;

public class C2SAddAreaPacket implements FortressC2SPacket {

    public static final String CHANNEL = "add_area";
    private final IAutomationAreaInfo automationAreaInfo;

    public C2SAddAreaPacket(IAutomationAreaInfo automationAreaInfo) {
        this.automationAreaInfo = automationAreaInfo;
    }

    public C2SAddAreaPacket(PacketByteBuf buf) {
        final var reader = NetworkingReadersRegistry.findReader(IAutomationAreaInfo.class);
        this.automationAreaInfo = reader.readBuffer(buf);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var provider = getManagersProvider(player);
        final var areasManager = provider.getAutomationAreaManager();
        areasManager.addArea(automationAreaInfo);
    }

    @Override
    public void write(PacketByteBuf buf) {
        automationAreaInfo.writeToBuffer(buf);
    }
}
