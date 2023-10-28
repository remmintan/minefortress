package net.remmintan.mods.minefortress.core.interfaces.selections;

public interface ISelectionType {
    ISelection generate();
    String getDisplayName();
    String getButtonText();

    int ordinal();
    String name();
}
