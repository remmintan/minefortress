package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.networking.interfaces.FortressC2SPacket;
import org.minefortress.entity.interfaces.ITargetedPawn;

public class C2SMoveTargetPacket implements FortressC2SPacket {

    public static final String CHANNEL = "move_target";

    private final BlockPos pos;
    private final int id;

    public C2SMoveTargetPacket(BlockPos pos, int id) {
        this.pos = pos.toImmutable();
        this.id = id;
    }

    public C2SMoveTargetPacket(PacketByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.id = buffer.readInt();
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var entity = player.getWorld().getEntityById(id);
        if(entity instanceof ITargetedPawn pawn) {
            pawn.setMoveTarget(pos);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(id);
    }
}
