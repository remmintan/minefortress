package org.minefortress.selections.renderer;

public interface ISelectionInfoProvider {

    boolean isSelecting();
    boolean isNeedsUpdate();
    void setNeedsUpdate(boolean needsUpdate);
    default boolean isInCorrectState() {
        return true;
    }

}
