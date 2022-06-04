package org.minefortress.fight;

public class ServerFightManager {

    private final ServerFightSelectionManager serverFightSelectionManager = new ServerFightSelectionManager();

    public ServerFightSelectionManager getServerFightSelectionManager() {
        return serverFightSelectionManager;
    }
}
