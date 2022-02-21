package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressClientPacket;

public class ClientboundSetBlueprintPacket implements FortressClientPacket {

    private final String blueprintFileName;
    private final NbtCompound blueprintTag;

    public ClientboundSetBlueprintPacket(String blueprintFileName, NbtCompound blueprintTag) {
        this.blueprintFileName = blueprintFileName;
        this.blueprintTag = blueprintTag;
    }

    public ClientboundSetBlueprintPacket(PacketByteBuf buf) {
        this.blueprintFileName = buf.readString();
        this.blueprintTag = buf.readNbt();
    }

    @Override
    public void handle(MinecraftClient client) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(blueprintFileName);
        buf.writeNbt(blueprintTag);
    }
}
