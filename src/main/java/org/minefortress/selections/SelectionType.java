package org.minefortress.selections;

import java.util.function.Supplier;

public enum SelectionType {

    SQUARES(TwoDotsSelection::new, "Squares", "SQ"),
    WALLS(WallsSelection::new, "Walls", "WA"),
    WALLS_EVERY_SECOND(WallsEverySecond::new, "Chess Walls", "CW"),
    LADDER(LadderSelection::new, "Ladder X Direction", "LX"),
    LADDER_Z_DIRECTION(LadderSelectionZDirection::new, "Ladder Z Direction", "LZ"),
    TREE(TreeSelection::new, "Trees", "TR"),
    ROADS(RoadsSelection::new, "Roads", "RO"),;

    private final Supplier<Selection> selectionGenerator;
    private final String name;
    private final String buttonText;

    SelectionType(Supplier<Selection> selectionGenerator, String name, String buttonText) {
        this.selectionGenerator = selectionGenerator;
        this.name = name;
        this.buttonText = buttonText;
    }

    public Selection generate() {
        return selectionGenerator.get();
    }

    public String getName() {
        return name;
    }

    public String getButtonText() {
        return buttonText;
    }
}
