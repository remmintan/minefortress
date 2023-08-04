package org.minefortress.fight;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.utils.ModUtils;

public class ClientFightManager {

    private final ClientFightSelectionManager selectionManager = new ClientFightSelectionManager();

    public ClientFightSelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void setTarget(HitResult hitResult) {
        if(hitResult instanceof BlockHitResult blockHitResult) {
            final var blockPos = blockHitResult.getBlockPos();
            selectionManager.forEachSelected(it -> it.setMoveTarget(blockPos));

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
