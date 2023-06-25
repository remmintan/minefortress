package org.minefortress.fortress.resources.gui.craft;

import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.gui.AbstractMissingProfessionScreen;

public class MissingCraftsmanScreen extends AbstractMissingProfessionScreen {

    @Override
    protected @NotNull String getMissingObjectName() {
        return "Craftsman";
    }

}
