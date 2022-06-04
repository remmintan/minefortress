package org.minefortress.fight;

import org.minefortress.entity.Colonist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerFightSelectionManager {

    private List<Colonist> selectedColonists = new ArrayList<>();

    public void selectColonists(List<Colonist> colonists) {
        selectedColonists = colonists;
    }

    public void clearSelection() {
        selectedColonists = Collections.emptyList();
    }

    public List<Colonist> getSelectedColonists() {
        return selectedColonists;
    }
}
