package org.minefortress.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

import java.util.UUID;

public class ServerboundSelectFightTargetPacket implements FortressServerPacket {

    private final TargetType targetType;
    private final BlockPos pos;
    private final Integer entityId;

    public ServerboundSelectFightTargetPacket(BlockPos pos) {
        this.targetType = TargetType.MOVE;
        this.pos = pos;
        this.entityId = null;
    }

    public ServerboundSelectFightTargetPacket(LivingEntity entity) {
        this.targetType = TargetType.ATTACK;
        this.pos = null;
        this.entityId = entity.getId();
    }

    public ServerboundSelectFightTargetPacket(PacketByteBuf buf) {
        this.targetType = buf.readEnumConstant(TargetType.class);
        if(targetType == TargetType.MOVE) {
            this.pos = buf.readBlockPos();
            this.entityId = null;
        } else {
            this.entityId = buf.readInt();
            this.pos = null;
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(targetType);
        if(targetType == TargetType.MOVE) {
            buf.writeBlockPos(pos);
        } else if(targetType == TargetType.ATTACK) {
            buf.writeInt(entityId);
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fightManager = this.getFortressServerManager(server, player).getServerFightManager();
        if(targetType == TargetType.MOVE) {
            fightManager.setMoveTarget(pos);
        } else if(targetType == TargetType.ATTACK) {
            final var entityById = (LivingEntity)player.world.getEntityById(entityId);
            fightManager.setAttackTarget(entityById);
        }
    }

    private enum TargetType {
        MOVE,
        ATTACK
    }

}
