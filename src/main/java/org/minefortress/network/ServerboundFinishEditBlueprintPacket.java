package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundFinishEditBlueprintPacket implements FortressServerPacket {

    private final boolean shouldSave;

    public ServerboundFinishEditBlueprintPacket(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public ServerboundFinishEditBlueprintPacket(PacketByteBuf buf) {
        this.shouldSave = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(this.shouldSave);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final ServerWorld world = server.getWorld(World.OVERWORLD);
        player.moveToWorld(world);
    }
}
