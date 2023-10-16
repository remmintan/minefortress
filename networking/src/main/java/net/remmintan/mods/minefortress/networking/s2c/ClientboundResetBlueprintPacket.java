package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

public class ClientboundResetBlueprintPacket implements FortressS2CPacket {

    public ClientboundResetBlueprintPacket() {}
    public ClientboundResetBlueprintPacket(PacketByteBuf buf) {}

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.get_BlueprintManager().reset();
        }
    }

    @Override
    public void write(PacketByteBuf buf) {}
}
