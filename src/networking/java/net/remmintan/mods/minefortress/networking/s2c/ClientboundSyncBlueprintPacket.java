package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

public class ClientboundSyncBlueprintPacket implements FortressS2CPacket {

    private final BlueprintMetadata metadata;
    private final NbtCompound tag;

    public ClientboundSyncBlueprintPacket(BlueprintMetadata metadata, NbtCompound tag) {
        this.metadata = metadata;
        this.tag = tag;
    }

    public ClientboundSyncBlueprintPacket(PacketByteBuf buf) {
        final var serializedMetadata = buf.readNbt();
        if (serializedMetadata == null) throw new IllegalArgumentException("Blueprint metadata is null");
        this.metadata = new BlueprintMetadata(serializedMetadata);
        this.tag = buf.readNbt();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var provider = getManagersProvider();
        final var blueprintManager = provider.get_BlueprintManager();
        blueprintManager.sync(metadata, tag);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeNbt(metadata.toNbt());
        buf.writeNbt(tag);
    }
}
