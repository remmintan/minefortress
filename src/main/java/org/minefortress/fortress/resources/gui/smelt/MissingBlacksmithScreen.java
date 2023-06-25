package org.minefortress.fortress.resources.gui.smelt;

import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.gui.AbstractMissingProfessionScreen;

public class MissingBlacksmithScreen extends AbstractMissingProfessionScreen {

    public MissingBlacksmithScreen(boolean missingBuilding) {
        super(missingBuilding);
    }

    @Override
    protected @NotNull String getMissingObjectName() {
        return "Blacksmith" + (missingBuilding ? "'s House" : "");
    }

}
