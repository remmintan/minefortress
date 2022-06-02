package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

public class ClientboundSyncCombatStatePacket implements FortressClientPacket {

    private final boolean isInCombat;

    public ClientboundSyncCombatStatePacket(boolean isInCombat) {
        this.isInCombat = isInCombat;
    }

    public ClientboundSyncCombatStatePacket(PacketByteBuf buf) {
        this.isInCombat = buf.readBoolean();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getFortressClientManager().setInCombat(isInCombat);
            if(isInCombat)
                fortressClient.setTicksSpeed(1);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(isInCombat);
    }
}
