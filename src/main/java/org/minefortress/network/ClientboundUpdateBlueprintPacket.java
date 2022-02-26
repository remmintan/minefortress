package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

public class ClientboundUpdateBlueprintPacket implements FortressClientPacket {

    private final String file;
    private final int newFloorLevel;
    private final NbtCompound tag;

    public ClientboundUpdateBlueprintPacket(String file, int newFloorLevel, NbtCompound tag) {
        this.file = file;
        this.newFloorLevel = newFloorLevel;
        this.tag = tag;
    }

    public ClientboundUpdateBlueprintPacket(PacketByteBuf buf) {
        file = buf.readString();
        newFloorLevel = buf.readInt();
        tag = buf.readNbt();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getBlueprintManager().update(file, tag, newFloorLevel);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(file);
        buf.writeInt(newFloorLevel);
        buf.writeNbt(tag);
    }
}
