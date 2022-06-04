package org.minefortress.fight;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;

import java.util.function.Consumer;

public class ServerFightManager {

    private final ServerFightSelectionManager serverFightSelectionManager = new ServerFightSelectionManager();

    public ServerFightSelectionManager getServerFightSelectionManager() {
        return serverFightSelectionManager;
    }

    public void setMoveTarget(BlockPos pos) {
        forEachSelectedColonist(c -> c.getFightControl().setMoveTarget(pos));
    }

    public void setAttackTarget(LivingEntity entity) {
        forEachSelectedColonist(c -> c.getFightControl().setAttackTarget(entity));
    }

    private void forEachSelectedColonist(Consumer<Colonist> consumer) {
        final var selectedColonists = serverFightSelectionManager.getSelectedColonists();
        selectedColonists.forEach(consumer);
    }

}
