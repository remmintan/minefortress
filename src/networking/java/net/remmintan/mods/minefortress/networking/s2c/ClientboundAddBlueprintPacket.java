package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

public class ClientboundAddBlueprintPacket implements FortressS2CPacket {

    private final BlueprintGroup group;
    private final String name;
    private final String fileName;
    private final int floorLevel;
    private final String requirementId;
    private final NbtCompound tag;

    public ClientboundAddBlueprintPacket(BlueprintGroup group, String name, String fileName, int floorLevel, String requirementId, NbtCompound tag) {
        this.group = group;
        this.name = name;
        this.fileName = fileName;
        this.tag = tag;
        this.floorLevel = floorLevel;
        this.requirementId = requirementId;
    }

    public ClientboundAddBlueprintPacket(PacketByteBuf buf) {
        this.group = buf.readEnumConstant(BlueprintGroup.class);
        this.name = buf.readString();
        this.fileName = buf.readString();
        this.tag = buf.readNbt();
        this.floorLevel = buf.readInt();
        this.requirementId = buf.readString();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var provider = getManagersProvider();
        final var blueprintManager = provider.get_BlueprintManager();
        blueprintManager.add(group, name, fileName, floorLevel, requirementId, tag);

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(group);
        buf.writeString(name);
        buf.writeString(fileName);
        buf.writeNbt(tag);
        buf.writeInt(floorLevel);
        buf.writeString(requirementId);
    }
}
