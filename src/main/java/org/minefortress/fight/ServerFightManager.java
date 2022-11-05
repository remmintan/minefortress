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

    private final List<LivingEntity> scaryMob = new ArrayList<>();

    public ServerFightSelectionManager getServerFightSelectionManager() {
        return serverFightSelectionManager;
    }

    public void tick() {
        scaryMob.removeIf(attacker -> !attacker.isAlive());
    }

    public void addScaryMob(@Nullable LivingEntity attacker) {
        if(attacker == null) return;
        if(!attacker.isAlive())return;
        if(scaryMob.contains(attacker))return;
        this.scaryMob.add(attacker);
    }

    public boolean hasAnyScaryMob() {
        return !scaryMob.isEmpty();
    }

    public LivingEntity getRandomScaryMob(Random random) {
        return scaryMob.get(random.nextInt(scaryMob.size()));
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
