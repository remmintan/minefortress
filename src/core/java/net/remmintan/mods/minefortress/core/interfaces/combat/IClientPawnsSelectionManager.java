package net.remmintan.mods.minefortress.core.interfaces.combat;

import net.minecraft.client.Mouse;
import net.minecraft.util.math.Vec2f;
import net.remmintan.mods.minefortress.core.dtos.combat.MousePos;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;

import java.util.Set;
import java.util.function.Consumer;

public interface IClientPawnsSelectionManager {
    void startSelection(double x, double y);

    void endSelection(double x, double y);

    boolean hasSelected();

    void updateSelection(Mouse mouse);

    void resetSelection();

    boolean isSelecting();

    boolean isSelectionStarted();

    void forEachSelected(Consumer<IFortressAwareEntity> action);

    MousePos getMouseStartPos();

    MousePos getMouseEndPos();

    boolean isSelected(IFortressAwareEntity colonist);

    Set<Vec2f> getScreenPositions();
}
