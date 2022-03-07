package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

public class ClientboundAddBlueprintPacket implements FortressClientPacket {

    private final BlueprintGroup group;
    private final String name;
    private final String fileName;
    private final int floorLevel;
    private final boolean premium;
    private final NbtCompound tag;

    public ClientboundAddBlueprintPacket(BlueprintGroup group, String name, String fileName, NbtCompound tag, int floorLevel, boolean premium) {
        this.group = group;
        this.name = name;
        this.fileName = fileName;
        this.tag = tag;
        this.floorLevel = floorLevel;
        this.premium = premium;
    }

    public ClientboundAddBlueprintPacket(PacketByteBuf buf) {
        this.group = buf.readEnumConstant(BlueprintGroup.class);
        this.name = buf.readString();
        this.fileName = buf.readString();
        this.tag = buf.readNbt();
        this.floorLevel = buf.readInt();
        this.premium = buf.readBoolean();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressMinecraftClient) {
            final ClientBlueprintManager blueprintManager = fortressMinecraftClient.getBlueprintManager();
            blueprintManager.add(group, name, fileName, floorLevel, tag, premium);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(group);
        buf.writeString(name);
        buf.writeString(fileName);
        buf.writeNbt(tag);
        buf.writeInt(floorLevel);
        buf.writeBoolean(premium);
    }
}
