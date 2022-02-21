package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

public class ClientboundEditBlueprintPacket implements FortressClientPacket {

    private final String file;
    private final NbtCompound tag;

    public ClientboundEditBlueprintPacket(String file, NbtCompound tag) {
        this.file = file;
        this.tag = tag;
    }

    public ClientboundEditBlueprintPacket(PacketByteBuf buf) {
        file = buf.readString();
        tag = buf.readNbt();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getBlueprintManager().update(file, tag);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(file);
        buf.writeNbt(tag);
    }
}
