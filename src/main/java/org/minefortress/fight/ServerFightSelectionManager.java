package org.minefortress.fight;

import org.minefortress.entity.interfaces.IWarriorPawn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerFightSelectionManager {

    private List<IWarriorPawn> selectedColonists = new ArrayList<>();

    public void selectColonists(List<IWarriorPawn> colonists) {
        selectedColonists = colonists;
    }

    public void clearSelection() {
        selectedColonists = Collections.emptyList();
    }

    public List<IWarriorPawn> getSelectedColonists() {
        return selectedColonists;
    }
}
