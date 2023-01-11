package org.minefortress.areas;

import com.google.common.collect.Streams;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class AreasClientManager {

    private ProfessionsSelectionType selectionType;

    private BlockPos selectionStart;
    private BlockPos selectionEnd;

    public boolean select(HitResult target) {
        if(target == null) return false;
        if(target instanceof BlockHitResult bhr) {
            final var blockPos = bhr.getBlockPos();
            if(selectionStart == null) {
                selectionStart = blockPos;
                selectionEnd = blockPos;
            }
        }
        return true;
    }

    public void updateSelection(HitResult crosshairTarget) {
        if(crosshairTarget instanceof BlockHitResult bhr) {
            final var blockPos = bhr.getBlockPos();
            if(selectionStart != null) {
                selectionEnd = blockPos;
            }
        }
    }

    public void resetSelection() {
        this.selectionEnd = null;
        this.selectionStart = null;
    }

    public boolean isSelecting() {
        return selectionStart != null;
    }

    public List<BlockPos> getSelection() {
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

}
