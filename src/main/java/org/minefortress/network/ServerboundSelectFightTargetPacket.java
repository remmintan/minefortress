package org.minefortress.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

import java.util.UUID;

public class ServerboundSelectFightTargetPacket implements FortressServerPacket {

    private final TargetType targetType;
    private final BlockPos pos;
    private final boolean setOnFire;
    private final BlockHitResult hit;
    private final Integer entityId;

    public ServerboundSelectFightTargetPacket(BlockPos pos, boolean setOnFire, BlockHitResult hit) {
        this.targetType = TargetType.MOVE;
        this.pos = pos;
        this.setOnFire = setOnFire;
        this.hit = hit;
        this.entityId = null;
    }

    public ServerboundSelectFightTargetPacket(LivingEntity entity) {
        this.targetType = TargetType.ATTACK;
        this.pos = null;
        this.setOnFire = false;
        this.hit = null;
        this.entityId = entity.getId();
    }

    public ServerboundSelectFightTargetPacket(PacketByteBuf buf) {
        this.targetType = buf.readEnumConstant(TargetType.class);
        if(targetType == TargetType.MOVE) {
            this.pos = buf.readBlockPos();
            this.setOnFire = buf.readBoolean();
            this.hit = buf.readBlockHitResult();
            this.entityId = null;
        } else {
            this.entityId = buf.readInt();
            this.setOnFire = false;
            this.hit = null;
            this.pos = null;
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(targetType);
        if(targetType == TargetType.MOVE) {
            buf.writeBlockPos(pos);
            buf.writeBoolean(setOnFire);
            buf.writeBlockHitResult(hit);
        } else if(targetType == TargetType.ATTACK) {
            buf.writeInt(entityId);
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServerManager = this.getFortressServerManager(server, player);
        final var fightManager = fortressServerManager.getServerFightManager();
        if(targetType == TargetType.MOVE) {
            fightManager.setMoveTarget(pos, setOnFire, hit);
        } else if(targetType == TargetType.ATTACK) {
            final var entityById = (LivingEntity)player.world.getEntityById(entityId);
            if(entityById instanceof Colonist colonist) {
                final var fortressId = colonist.getFortressId();
                if(fortressId != null && fortressId.equals(fortressServerManager.getId())) return;
            }
            fightManager.setAttackTarget(entityById);
        }
    }

    private enum TargetType {
        MOVE,
        ATTACK
    }

}
