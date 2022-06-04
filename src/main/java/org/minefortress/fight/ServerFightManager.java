package org.minefortress.fight;

import net.minecraft.util.math.BlockPos;

public class ServerFightManager {

    private final ServerFightSelectionManager serverFightSelectionManager = new ServerFightSelectionManager();

    public ServerFightSelectionManager getServerFightSelectionManager() {
        return serverFightSelectionManager;
    }

    public void setMoveTarget(BlockPos pos) {
        final var selectedColonists = serverFightSelectionManager.getSelectedColonists();
        selectedColonists.forEach(c -> c.getFightControl().setMoveTarget(pos));
    }

}
