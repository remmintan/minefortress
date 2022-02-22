package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

public class ClientboundResetBlueprintPacket implements FortressClientPacket {

    public ClientboundResetBlueprintPacket() {}
    public ClientboundResetBlueprintPacket(PacketByteBuf buf) {}

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getBlueprintManager().reset();
        }
    }

    @Override
    public void write(PacketByteBuf buf) {}
}
