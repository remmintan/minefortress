package org.minefortress.fortress;

import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderStage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FortressBorder extends WorldBorder {

    private boolean hasDynamicStage = false;
    private final List<WorldBorder> additionalBorders = new ArrayList<>();

    public void addAdditionalBorder(WorldBorder worldBorder) {
        additionalBorders.add(worldBorder);
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
