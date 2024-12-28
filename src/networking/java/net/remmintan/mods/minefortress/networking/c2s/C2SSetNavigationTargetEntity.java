package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class C2SSetNavigationTargetEntity implements FortressC2SPacket {

    public static final String CHANNEL = "set_navigation_target_entity";

    private final BlockPos pos;

    public C2SSetNavigationTargetEntity(BlockPos pos) {
        this.pos = pos;
    }

    public C2SSetNavigationTargetEntity(PacketByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var provider = getManagersProvider(player);
        final var fightManager = provider.getFightManager();
        fightManager.setCurrentTarget(pos, player.getServerWorld());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
