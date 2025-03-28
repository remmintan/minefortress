package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import org.jetbrains.annotations.NotNull;

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
    public void handle(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player) {
        final var entity = player.getWorld().getEntityById(id);
        final var target = player.getWorld().getEntityById(targetId);
        if(entity instanceof ITargetedPawn pawn && target instanceof LivingEntity targetEntity) {
            pawn.setAttackTarget(targetEntity);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(targetId);
    }
}
