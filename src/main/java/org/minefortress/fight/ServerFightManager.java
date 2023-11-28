package org.minefortress.fight;

import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.combat.IServerFightManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import org.minefortress.entity.fight.NavigationTargetEntity;
import org.minefortress.registries.FortressEntities;

import java.util.UUID;

public class ServerFightManager implements IServerFightManager, IWritableManager {

    private NavigationTargetEntity oldTarget;
    private UUID oldTargetUuid;

    @Override
    public void setCurrentTarget(BlockPos pos, ServerWorld world) {
        keepTrackOfOldTarget(world);
        oldTarget = FortressEntities.NAVIGATION_TARGET_ENTITY_TYPE.spawn(world, pos.up(), SpawnReason.EVENT);
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
}
