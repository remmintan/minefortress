package org.minefortress.selections;

import java.util.function.Supplier;

public enum SelectionType {

    SQUARES(TwoDotsSelection::new, "Squares"),
    WALLS(WallsSelection::new, "Walls"),
    WALLS_EVERY_SECOND(WallsEverySecond::new, "Chess Walls"),
    LADDER(LadderSelection::new, "Ladder X Direction"),
    LADDER_Z_DIRECTION(LadderSelectionZDirection::new, "Ladder Z Direction");

    private final Supplier<Selection> selectionGenerator;
    private final String name;

    SelectionType(Supplier<Selection> selectionGenerator, String name) {
        this.selectionGenerator = selectionGenerator;
        this.name = name;
    }

    public Selection generate() {
        return selectionGenerator.get();
    }

    public String getName() {
        return name;
    }
}
