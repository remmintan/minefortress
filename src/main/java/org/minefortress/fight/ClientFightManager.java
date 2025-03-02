package org.minefortress.fight;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.ITargetedSelectionManager;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.networking.c2s.C2SAttractWarriorsToCampfire;
import net.remmintan.mods.minefortress.networking.c2s.C2SSetNavigationTargetEntity;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.entity.Colonist;

public class ClientFightManager implements IClientFightManager {
    private int warriorCount;

    @Override
    public void setTarget(HitResult hitResult, ITargetedSelectionManager targetedSelectionManager) {
        if(hitResult instanceof BlockHitResult blockHitResult) {
            final var blockPos = blockHitResult.getBlockPos();
            targetedSelectionManager.forEachTargetedPawn(it -> it.setMoveTarget(blockPos));
            final var packet = new C2SSetNavigationTargetEntity(blockPos);
            FortressClientNetworkHelper.send(C2SSetNavigationTargetEntity.CHANNEL, packet);
        }
    }

    @Override
    public void setTarget(Entity entity, ITargetedSelectionManager targetedSelectionManager) {
        if(!(entity instanceof LivingEntity livingEntity)) return;
        if (entity instanceof Colonist pawn) {
            final var manager = ClientModUtils.getFortressCenterManager();
            if (manager.hasTheSameCenter(pawn))
                return;
        }
        targetedSelectionManager.forEachTargetedPawn(it -> it.setAttackTarget(livingEntity));
    }

    @Override
    public void sync(int count) {
        this.warriorCount = count;
    }

    @Override
    public int getWarriorCount() {
        return warriorCount;
    }

    @Override
    public void attractWarriorsToCampfire() {
        final var packet = new C2SAttractWarriorsToCampfire();
        FortressClientNetworkHelper.send(C2SAttractWarriorsToCampfire.CHANNEL, packet);
    }
}
