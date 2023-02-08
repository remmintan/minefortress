package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.automation.areas.AutomationAreaInfo;
import org.minefortress.network.interfaces.FortressC2SPacket;

public class C2SAddAreaPacket implements FortressC2SPacket {

    public static final String CHANNEL = "add_area";
    private final AutomationAreaInfo automationAreaInfo;

    public C2SAddAreaPacket(AutomationAreaInfo automationAreaInfo) {
        this.automationAreaInfo = automationAreaInfo;
    }

    public C2SAddAreaPacket(PacketByteBuf buf) {
        this.automationAreaInfo = AutomationAreaInfo.readFromBuffer(buf);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var areasManager = getFortressServerManager(server, player).getAreasManager();
        areasManager.addArea(automationAreaInfo);
    }

    @Override
    public void write(PacketByteBuf buf) {
        automationAreaInfo.writeToBuffer(buf);
    }
}
