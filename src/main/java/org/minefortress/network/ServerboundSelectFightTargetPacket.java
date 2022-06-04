package org.minefortress.network;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fight.ServerFightManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundSelectFightTargetPacket implements FortressServerPacket {

    private final TargetType targetType;
    private final BlockPos pos;
    private final LivingEntity entity;

    public ServerboundSelectFightTargetPacket(BlockPos pos) {
        this.targetType = TargetType.MOVE;
        this.pos = pos;
        this.entity = null;
    }

    public ServerboundSelectFightTargetPacket(LivingEntity entity) {
        this.targetType = TargetType.ATTACK;
        this.pos = null;
        this.entity = entity;
    }

    public ServerboundSelectFightTargetPacket(PacketByteBuf buf) {
        this.targetType = buf.readEnumConstant(TargetType.class);
        if(targetType == TargetType.MOVE) {
            this.pos = buf.readBlockPos();
            this.entity = null;
        } else {
            throw new IllegalArgumentException("Invalid target type");
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(targetType);
        if(targetType == TargetType.MOVE) {
            buf.writeBlockPos(pos);
        } else if(targetType == TargetType.ATTACK) {
            throw new IllegalArgumentException("Not implemented yet");
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(player instanceof FortressServerPlayerEntity fortressPlayer) {
            final var fightManager = fortressPlayer.getFortressServerManager().getServerFightManager();
            if(targetType == TargetType.MOVE) {
                fightManager.setMoveTarget(pos);
            }
        }
    }

    private enum TargetType {
        MOVE,
        ATTACK
    }

}
