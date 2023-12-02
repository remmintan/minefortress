package net.remmintan.mods.minefortress.core.interfaces.networking;

import net.minecraft.client.MinecraftClient;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;

public interface FortressS2CPacket extends FortressPacket{
    void handle(MinecraftClient client);

    default IClientManagersProvider getManagersProvider() {
        final var client = MinecraftClient.getInstance();
        if(client instanceof IClientManagersProvider provider) {
            return  provider;
        }

        throw new IllegalStateException("MinecraftClient is not an instance of IClientManagersProvider");
    }

}
