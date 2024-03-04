package net.remmintan.mods.minefortress.core.interfaces.combat;

import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;

import java.util.function.Consumer;

public interface ITargetedSelectionManager {

    void forEachTargetedPawn(Consumer<ITargetedPawn> action);

}
