package org.minefortress.fight;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.minefortress.entity.Colonist;
import org.minefortress.network.ServerboundSelectFightTargetPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

public class ClientFightManager {

    private final ClientFightSelectionManager selectionManager = new ClientFightSelectionManager();

    public ClientFightSelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void setTarget(HitResult hitResult) {
        if(hitResult instanceof BlockHitResult blockHitResult) {
            final var blockPos = blockHitResult.getBlockPos();
            final var packet = new ServerboundSelectFightTargetPacket(blockPos);
            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SELECT_FIGHT_TARGET, packet);
        } else if (hitResult instanceof EntityHitResult entityHitResult) {
            final var entity = entityHitResult.getEntity();
            if(!(entity instanceof LivingEntity livingEntity)) return;
            if(entity instanceof Colonist) return;
            final var packet = new ServerboundSelectFightTargetPacket(livingEntity);
            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SELECT_FIGHT_TARGET, packet);
        }
    }

    public void setTarget(Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity)) return;
        if(entity instanceof Colonist) return;
        final var packet = new ServerboundSelectFightTargetPacket(livingEntity);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SELECT_FIGHT_TARGET, packet);
    }
}
