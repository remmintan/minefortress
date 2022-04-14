package org.minefortress.interfaces;

import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.blueprints.renderer.BlueprintRenderer;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.renderer.gui.FortressHud;
import org.minefortress.selections.SelectionManager;
import org.minefortress.selections.renderer.campfire.CampfireRenderer;
import org.minefortress.selections.renderer.selection.SelectionRenderer;

public interface FortressMinecraftClient {

    SelectionManager getSelectionManager();
    FortressHud getFortressHud();

    ClientBlueprintManager getBlueprintManager();
    BlueprintRenderer getBlueprintRenderer();
    CampfireRenderer getCampfireRenderer();
    SelectionRenderer getSelectionRenderer();

    FortressClientManager getFortressClientManager();

    boolean isNotFortressGamemode();
    boolean isFortressGamemode();
    BlockPos getHoveredBlockPos();

    boolean isSupporter();

    void setTicksSpeed(int ticksSpeed);
    int getTicksSpeed();

}
