package org.minefortress.fortress.resources.gui.smelt;

import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.gui.AbstractMissingProfessionScreen;

public class MissingBlacksmithScreen extends AbstractMissingProfessionScreen {

    @Override
    protected @NotNull String getMissingProfession() {
        return "Blacksmith";
    }

}
