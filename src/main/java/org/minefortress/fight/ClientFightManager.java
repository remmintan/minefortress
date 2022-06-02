package org.minefortress.fight;

public class ClientFightManager {

    private final ClientFightSelectionManager selectionManager = new ClientFightSelectionManager();

    public ClientFightSelectionManager getSelectionManager() {
        return selectionManager;
    }
}
