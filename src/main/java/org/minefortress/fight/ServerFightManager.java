package org.minefortress.fight;

import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.combat.IServerFightManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.S2CSyncFightManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.fight.NavigationTargetEntity;
import org.minefortress.registries.FortressEntities;

import java.util.UUID;

public class ServerFightManager implements IServerFightManager {

    private final BlockPos fortressPos;
    private IServerFortressManager serverFortressManager;
    private NavigationTargetEntity oldTarget;
    private UUID oldTargetUuid;

    private boolean syncNeeded = false;

    public ServerFightManager(BlockPos fortressPos) {
        this.fortressPos = fortressPos;
    }

    @Override
    public void spawnDebugWarriors(int num, ServerPlayerEntity player) {
        serverFortressManager.spawnDebugEntitiesAroundCampfire(FortressEntities.WARRIOR_PAWN_ENTITY_TYPE, num, player);
    }

    @Override
    public void setCurrentTarget(BlockPos pos, ServerWorld world) {
        keepTrackOfOldTarget(world);
        oldTarget = FortressEntities.NAVIGATION_TARGET_ENTITY_TYPE.spawn(world, pos.up(), SpawnReason.EVENT);
    }

    @Override
    public void attractWarriorsToCampfire() {
        final var fortressCenter = serverFortressManager.getFortressCenter();
        serverFortressManager.getAllTargetedPawns().forEach(it -> it.setMoveTarget(fortressCenter));
    }

    @Override
    public void sync() {
        syncNeeded = true;
    }

    private void keepTrackOfOldTarget(ServerWorld world) {
        if(oldTargetUuid != null) {
            final var entity = world.getEntity(oldTargetUuid);
            if(entity instanceof NavigationTargetEntity navigationTarget)
                oldTarget = navigationTarget;
        }


        if(oldTarget != null) {
            if(!oldTarget.isRemoved())
                oldTarget.discard();
        }
    }

    @Override
    public void write(NbtCompound tag) {
        if (oldTarget != null)
            tag.putUuid("oldTarget", oldTarget.getUuid());
    }

    @Override
    public void read(NbtCompound tag) {
        if (tag.contains("oldTarget")) {
            oldTargetUuid = tag.getUuid("oldTarget");
        }
    }

    @Override
    public void tick(@NotNull MinecraftServer server, @NotNull ServerWorld world, @Nullable ServerPlayerEntity player) {
        if (serverFortressManager == null) {
            serverFortressManager = ServerModUtils.getFortressManager(server, fortressPos);
        }
        if(player==null) return;
        if(syncNeeded) {
            final var packet = new S2CSyncFightManager(countAllWarriors());
            FortressServerNetworkHelper.send(player, S2CSyncFightManager.CHANNEL, packet);
            this.syncNeeded = false;
        }
    }

    private int countAllWarriors() {
        return serverFortressManager.getAllTargetedPawns().size();
    }
}
