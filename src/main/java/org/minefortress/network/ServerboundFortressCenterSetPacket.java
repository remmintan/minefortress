package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundFortressCenterSetPacket implements FortressServerPacket {

    private final BlockPos pos;

    public ServerboundFortressCenterSetPacket(BlockPos pos) {
        this.pos = pos;
    }

    public ServerboundFortressCenterSetPacket(PacketByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServerManager = this.getFortressServerManager(server, player);
        fortressServerManager.setupCenter(pos, player.world, player);
    }
}
