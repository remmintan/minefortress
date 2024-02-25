package net.remmintan.mods.minefortress.core.interfaces.combat;

import net.minecraft.client.Mouse;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.dtos.combat.MousePos;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

public interface IClientFightSelectionManager {
    void startSelection(double x, double y);

    void endSelection();

    boolean hasSelected();

    void updateSelection(Mouse mouse, BlockHitResult target);

    void updateSelection(double x, double y);

    void resetSelection();

    boolean isSelecting();

    boolean isSelectionStarted();

    void forEachSelected(Consumer<ITargetedPawn> action);

    MousePos getMouseStartPos();

    MousePos getMouseEndPos();

    boolean isSelected(IFortressAwareEntity colonist);

    Set<Vec2f> getScreenPositions();
}
