package org.minefortress.mixins.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.gui.OutdatedWorldScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class FortressClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {

    public FortressClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Override
    protected Screen createDisconnectedScreen(Text reason) {
        if (reason != null && reason.getString().contains("Outdated world version")) {
            return new OutdatedWorldScreen();
        }
        return super.createDisconnectedScreen(reason);
    }
}
