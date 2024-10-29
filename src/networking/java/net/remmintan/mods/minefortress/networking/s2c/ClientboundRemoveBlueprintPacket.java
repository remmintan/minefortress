package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

public class ClientboundRemoveBlueprintPacket implements FortressS2CPacket {

    private final String blueprintId;

    public ClientboundRemoveBlueprintPacket(String blueprintId) {
        this.blueprintId = blueprintId;
    }

    public ClientboundRemoveBlueprintPacket(PacketByteBuf buf) {
        blueprintId = buf.readString();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var provider = getManagersProvider();
        final var blueprintManager = provider.get_BlueprintManager();

        blueprintManager.clearStructure();
        blueprintManager.remove(blueprintId);
        blueprintManager.updateSlotsInBlueprintsScreen();

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(blueprintId);
    }

}
