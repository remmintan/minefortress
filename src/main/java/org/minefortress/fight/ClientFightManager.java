package org.minefortress.fight;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightSelectionManager;
import net.remmintan.mods.minefortress.networking.c2s.C2SSetNavigationTargetEntity;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.entity.Colonist;
import org.minefortress.utils.ModUtils;

public class ClientFightManager implements IClientFightManager {

    private final IClientFightSelectionManager selectionManager = new ClientFightSelectionManager();

    @Override
    public IClientFightSelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public void setTarget(HitResult hitResult) {
        if(hitResult instanceof BlockHitResult blockHitResult) {
            final var blockPos = blockHitResult.getBlockPos();
            selectionManager.forEachSelected(it -> it.setMoveTarget(blockPos));
            final var packet = new C2SSetNavigationTargetEntity(blockPos);
            FortressClientNetworkHelper.send(C2SSetNavigationTargetEntity.CHANNEL, packet);
        } else if (hitResult instanceof EntityHitResult entityHitResult) {
            final var entity = entityHitResult.getEntity();
            if(!(entity instanceof LivingEntity livingEntity)) return;
            if(entity instanceof Colonist col) {
                final var masterPlayerId = col.getMasterId();
                if(masterPlayerId.map(it -> it.equals(ModUtils.getCurrentPlayerUUID())).orElse(false))
                    return;
            }
            selectionManager.forEachSelected(it -> it.setAttackTarget(livingEntity));
        }
    }

    @Override
    public void setTarget(Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity)) return;
        if(entity instanceof Colonist col) {
            final var masterPlayerId = col.getMasterId();
            final var playerId= ModUtils.getCurrentPlayerUUID();
            if(masterPlayerId.map(it -> it.equals(playerId)).orElse(false))
                return;
        }
        selectionManager.forEachSelected(it -> it.setAttackTarget(livingEntity));
    }
}
