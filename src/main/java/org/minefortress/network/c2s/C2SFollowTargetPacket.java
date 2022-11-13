package org.minefortress.network.c2s;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.entity.interfaces.IWarriorPawn;
import org.minefortress.network.interfaces.FortressC2SPacket;

public class C2SFollowTargetPacket implements FortressC2SPacket {

    public static final String CHANNEL = "follow_target";

    private final int id;
    private final int targetId;

    public C2SFollowTargetPacket(int id, int targetId) {
        this.id = id;
        this.targetId = targetId;
    }

    public C2SFollowTargetPacket(PacketByteBuf buffer) {
        this.id = buffer.readInt();
        this.targetId = buffer.readInt();
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var entity = player.world.getEntityById(id);
        final var target = player.world.getEntityById(targetId);
        if(entity instanceof IWarriorPawn pawn && target instanceof LivingEntity targetEntity) {
            pawn.setAttackTarget(targetEntity);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(targetId);
    }
}
