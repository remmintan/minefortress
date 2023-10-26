package net.remmintan.mods.minefortress.core.interfaces.selections;

public interface ISelectionType {
    ISelection generate();

    String getName();

    String getButtonText();

    int ordinal();
}
