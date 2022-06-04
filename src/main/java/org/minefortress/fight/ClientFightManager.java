package org.minefortress.fight;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
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
        }
    }
}
