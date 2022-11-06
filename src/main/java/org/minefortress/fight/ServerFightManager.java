package org.minefortress.fight;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.IWarriorPawn;

import java.util.*;
import java.util.function.Consumer;

public class ServerFightManager {

    private final ServerFightSelectionManager serverFightSelectionManager = new ServerFightSelectionManager();

    public ServerFightSelectionManager getServerFightSelectionManager() {
        return serverFightSelectionManager;
    }

    public void setMoveTarget(BlockPos pos, boolean setOnFire, BlockHitResult hit) {
        if(setOnFire) {
            forEachSelectedColonist(it -> it.getFightControl().setFireTarget(hit));
        } else {
            forEachSelectedColonist(c -> c.getFightControl().setMoveTarget(pos));
        }
    }

    public void setAttackTarget(LivingEntity entity) {
        forEachSelectedColonist(c -> c.getFightControl().setAttackTarget(entity));
    }

    private void forEachSelectedColonist(Consumer<IWarriorPawn> consumer) {
        final var selectedColonists = serverFightSelectionManager.getSelectedColonists();
        selectedColonists.forEach(consumer);
    }

}
