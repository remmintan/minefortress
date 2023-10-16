package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import org.jetbrains.annotations.NotNull;

public class C2SUpdateNewInfluencePosition implements FortressC2SPacket {

    public static final String CHANNEL = "update_new_influence_position";
    private final BlockPos pos;

    public C2SUpdateNewInfluencePosition(@NotNull BlockPos pos) {
        this.pos = pos;
    }

    public C2SUpdateNewInfluencePosition(PacketByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServerManager = getManagersProvider(server, player);
        final var influenceManager = fortressServerManager.getInfluenceManager();
        influenceManager.checkNewPositionAndUpdateClientState(pos, player);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
