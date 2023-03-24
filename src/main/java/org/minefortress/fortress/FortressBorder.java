package org.minefortress.fortress;

import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderStage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FortressBorder extends WorldBorder {

    private boolean hasDynamicStage = false;
    private final List<WorldBorder> additionalBorders = new ArrayList<>();
    private final List<BoundPosition> uniqueBorders = new ArrayList<>();

    public void addAdditionalBorder(WorldBorder worldBorder) {
        additionalBorders.add(worldBorder);
        recalculateUniqueBorders();
    }

    private void recalculateUniqueBorders() {
        uniqueBorders.clear();
        final var thisBorderPositions = Arrays.asList(
                new BoundPosition(getBoundWest(), getCenterZ()),
                new BoundPosition(getBoundEast(), getCenterZ()),
                new BoundPosition(getCenterX(), getBoundNorth()),
                new BoundPosition(getCenterX(), getBoundSouth())
        );
        final var nonUniqueBorders = new ArrayList<>(thisBorderPositions);

        for (WorldBorder additionalBorder : additionalBorders) {
            nonUniqueBorders.add(new BoundPosition(additionalBorder.getBoundWest(), additionalBorder.getCenterZ()));
            nonUniqueBorders.add(new BoundPosition(additionalBorder.getBoundEast(), additionalBorder.getCenterZ()));
            nonUniqueBorders.add(new BoundPosition(additionalBorder.getCenterX(), additionalBorder.getBoundNorth()));
            nonUniqueBorders.add(new BoundPosition(additionalBorder.getCenterX(), additionalBorder.getBoundSouth()));
        }

        nonUniqueBorders.removeIf(it -> Collections.frequency(nonUniqueBorders, it) > 1);

        uniqueBorders.addAll(nonUniqueBorders);
    }

    public boolean shouldRenderBound(@Nullable Double boundX, @Nullable Double boundZ) {
        return boundX != null && boundZ != null && uniqueBorders.contains(new BoundPosition(boundX, boundZ));
    }

    @Override
    public WorldBorderStage getStage() {
        if(!hasDynamicStage) {
            return super.getStage();
        }
        return WorldBorderStage.SHRINKING;
    }

    public void enableDynamicStage() {
        this.hasDynamicStage = true;
    }

    public List<WorldBorder> getAdditionalBorders() {
        return Collections.unmodifiableList(additionalBorders);
    }

    private record BoundPosition(double x, double z) {}
}
