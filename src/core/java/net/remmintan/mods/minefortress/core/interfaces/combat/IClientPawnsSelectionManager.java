package net.remmintan.mods.minefortress.core.interfaces.combat;

import net.minecraft.client.Mouse;
import net.remmintan.mods.minefortress.core.dtos.combat.MousePos;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;

import java.util.List;
import java.util.function.Consumer;

public interface IClientPawnsSelectionManager {

    void selectSingle(IFortressAwareEntity fortressAwareEntity);
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

    List<Integer> getSelectedPawnsIds();
}
