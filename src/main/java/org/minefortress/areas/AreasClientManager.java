package org.minefortress.areas;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vector4f;
import org.minefortress.selections.renderer.ISelectionInfoProvider;
import org.minefortress.selections.renderer.ISelectionModelBuilderInfoProvider;

import java.util.Collections;
import java.util.List;

public final class AreasClientManager implements ISelectionInfoProvider, ISelectionModelBuilderInfoProvider {

    private boolean needsUpdate;

    private ProfessionsSelectionType selectionType;

    private BlockPos selectionStart;
    private BlockPos selectionEnd;

    private List<AutomationAreaInfo> savedAreas;

    public boolean select(HitResult target) {
        if(target == null) return false;
        if(target instanceof BlockHitResult bhr) {
            final var blockPos = bhr.getBlockPos();
            if(selectionStart == null) {
                this.needsUpdate = true;
                selectionStart = blockPos;
                selectionEnd = blockPos;
            } else {

            }
        }
        return true;
    }

    public void updateSelection(HitResult crosshairTarget) {
        if(crosshairTarget instanceof BlockHitResult bhr) {
            final var blockPos = bhr.getBlockPos();
            if(selectionStart != null) {
                if(blockPos != null && !blockPos.equals(selectionEnd)) {
                    selectionEnd = blockPos;
                    needsUpdate = true;
                }
            }
        }
    }

    public void resetSelection() {
        this.selectionEnd = null;
        this.selectionStart = null;
        this.needsUpdate = true;
    }

    public boolean isSelecting() {
        return selectionStart != null;
    }

    @Override
    public boolean isNeedsUpdate() {
        return this.needsUpdate;
    }

    @Override
    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    @Override
    public List<BlockPos> getSelectedBlocks() {
        if(selectionStart == null || selectionEnd == null) return List.of();
        return Streams
                .stream(BlockPos.iterate(selectionStart, selectionEnd))
                .map(BlockPos::toImmutable)
                .toList();
    }

    public ProfessionsSelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(ProfessionsSelectionType selectionType) {
        this.selectionType = selectionType;
    }

    @Override
    public Vector4f getClickColor() {
        return new Vector4f(0.0f, 0.0f, 1.0f, 1f);
    }
    @Override
    public List<Pair<Vec3i, Vec3i>> getSelectionDimensions() {
        return Collections.emptyList();
    }

    public void setSavedAreas(List<AutomationAreaInfo> savedAreas) {
        this.savedAreas = Collections.unmodifiableList(savedAreas);
    }
}
