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

    private final List<Double> uniqueBorders = new ArrayList<>();

    public void addAdditionalBorder(WorldBorder worldBorder) {
        additionalBorders.add(worldBorder);
        recalculateUniqueBorders();
    }

    private void recalculateUniqueBorders() {
        uniqueBorders.clear();
        final var thisBorderPositions = Arrays.asList(
                getBoundWest(),
                getBoundEast(),
                getBoundNorth(),
                getBoundSouth()
        );
        final var nonUniqueBorders = new ArrayList<>(thisBorderPositions);

        for (WorldBorder additionalBorder : additionalBorders) {
            nonUniqueBorders.add(additionalBorder.getBoundWest());
            nonUniqueBorders.add(additionalBorder.getBoundEast());
            nonUniqueBorders.add(additionalBorder.getBoundNorth());
            nonUniqueBorders.add(additionalBorder.getBoundSouth());
        }

        nonUniqueBorders.removeIf(it -> Collections.frequency(nonUniqueBorders, it) > 1);

        uniqueBorders.addAll(nonUniqueBorders);
    }

    public boolean shouldRenderBound(@Nullable Double bound) {
        return bound != null && uniqueBorders.contains(bound);
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
}
