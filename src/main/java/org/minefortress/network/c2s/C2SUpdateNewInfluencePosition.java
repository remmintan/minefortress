package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fight.influence.ServerInfluenceManager;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.network.interfaces.FortressC2SPacket;

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
        final var fortressServerManager = getFortressServerManager(server, player);
        final var influenceManager = fortressServerManager.getInfluenceManager();
        influenceManager.checkNewPositionAndUpdateClientState(pos, player);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
