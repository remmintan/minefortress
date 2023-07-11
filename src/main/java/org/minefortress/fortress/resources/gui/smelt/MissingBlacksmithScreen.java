package org.minefortress.fortress.resources.gui.smelt;

import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.gui.AbstractMissingProfessionScreen;

public class MissingBlacksmithScreen extends AbstractMissingProfessionScreen {
    public MissingBlacksmithScreen(boolean missingBuilding) {
        super(missingBuilding);
    }

    @Override
    protected @NotNull String getMissingObjectName() {
        return this.irregularReson? "house with a furnace" : "blacksmith";
    }

    @Override
    protected String getActionText() {
        return "Go to blueprints menu and build one";
    }
}
