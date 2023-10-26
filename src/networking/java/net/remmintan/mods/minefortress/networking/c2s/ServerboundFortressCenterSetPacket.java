package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class ServerboundFortressCenterSetPacket implements FortressC2SPacket {

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
        final var fortressServerManager = getFortressManager(server, player);
        fortressServerManager.setupCenter(pos, player.getWorld(), player);
    }
}
