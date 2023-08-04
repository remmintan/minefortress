package org.minefortress.fortress;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderStage;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fight.influence.ClientInfluenceManager;
import org.minefortress.utils.ModUtils;

import java.util.*;

public final class FortressBorder extends WorldBorder {

    private boolean hasDynamicStage = false;
    private final Set<WorldBorder> additionalBorders = new HashSet<>();
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
        return additionalBorders.isEmpty() ||
                boundX != null && boundZ != null &&
                uniqueBorders.contains(new BoundPosition(boundX, boundZ));
    }

    @Override
    public WorldBorderStage getStage() {
        if(!hasDynamicStage) {
            return super.getStage();
        }
        final var fortressClientManager = ModUtils.getFortressClientManager();
        if(fortressClientManager.isCenterNotSet()) {
            return WorldBorderStage.GROWING;
        }
        final var influenceManager = ModUtils.getInfluenceManager();
        return influenceManager.getInfluencePosStateHolder().getWorldBorderStage();
    }

    @Override
    public boolean contains(BlockPos pos) {
        if(super.contains(pos)) {
            return true;
        }
        final var ab = this.getAdditionalBorders();
        for (WorldBorder additionalBorder : ab) {
            if(additionalBorder.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    public void enableDynamicStage() {
        this.hasDynamicStage = true;
    }

    public Set<WorldBorder> getAdditionalBorders() {
        return Collections.unmodifiableSet(additionalBorders);
    }

    private record BoundPosition(double x, double z) {}
}
