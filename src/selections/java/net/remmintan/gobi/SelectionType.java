package net.remmintan.gobi;

import net.remmintan.mods.minefortress.core.interfaces.selections.ISelection;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionType;

import java.util.function.Supplier;

public enum SelectionType implements ISelectionType {

    SQUARES(TwoDotsSelection::new, "Squares", "SQ"),
    WALLS(WallsSelection::new, "Walls", "WA"),
    WALLS_EVERY_SECOND(WallsEverySecond::new, "Chess Walls", "CW"),
    LADDER(LadderSelection::new, "Ladder X Direction", "LX"),
    LADDER_Z_DIRECTION(LadderSelectionZDirection::new, "Ladder Z Direction", "LZ"),
    TREE(TreeSelection::new, "Trees", "TR"),
    ROADS(RoadsSelection::new, "Roads", "RO"),;

    private final Supplier<ISelection> selectionGenerator;
    private final String name;
    private final String buttonText;

    SelectionType(Supplier<ISelection> selectionGenerator, String name, String buttonText) {
        this.selectionGenerator = selectionGenerator;
        this.name = name;
        this.buttonText = buttonText;
    }

    @Override
    public ISelection generate() {
        return selectionGenerator.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getButtonText() {
        return buttonText;
    }
}
