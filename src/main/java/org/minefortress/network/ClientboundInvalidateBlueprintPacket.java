package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

public class ClientboundInvalidateBlueprintPacket implements FortressClientPacket {

    private final String fileName;

    public ClientboundInvalidateBlueprintPacket(String fileName) {
        this.fileName = fileName;
    }

    public ClientboundInvalidateBlueprintPacket(PacketByteBuf buf) {
        this.fileName = buf.readString(32767);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.fileName);
    }

    @Override
    public void handle(MinecraftClient client) {
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
        fortressClient.getBlueprintBlockDataManager().invalidateBlueprint(fileName);
        fortressClient.getBlueprintRenderer().getBlueprintsModelBuilder().invalidateBlueprint(fileName);
    }
}
