package net.remmintan.mods.minefortress.core.interfaces.selections;

public interface ISelectionInfoProvider {

    boolean isSelecting();
    boolean isNeedsUpdate();
    void setNeedsUpdate(boolean needsUpdate);
    default boolean isInCorrectState() {
        return true;
    }

}
