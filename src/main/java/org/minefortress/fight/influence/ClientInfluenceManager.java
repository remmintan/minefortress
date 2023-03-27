package org.minefortress.fight.influence;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientInfluenceManager {

    private List<BlockPos> allInfluencePositions = new ArrayList<>();
    private BlockPos currentPossibleInfluencePosition = null;
    private boolean isSelectingInfluencePosition = false;

    public void addInfluencePosition(BlockPos pos) {
        allInfluencePositions.add(pos);
    }

    public void removeInfluencePosition(BlockPos pos) {
        allInfluencePositions.remove(pos);
    }

    public List<BlockPos> getAllInfluencePositions() {
        return Collections.unmodifiableList(allInfluencePositions);
    }

    public void startSelectingInfluencePosition() {
        isSelectingInfluencePosition = true;
    }

    public void cancelSelectingInfluencePosition() {
        isSelectingInfluencePosition = false;
        currentPossibleInfluencePosition = null;
    }

    public void selectInfluencePosition() {
        isSelectingInfluencePosition = false;
        if(currentPossibleInfluencePosition != null) {
            addInfluencePosition(currentPossibleInfluencePosition);
            currentPossibleInfluencePosition = null;
        }
    }

}
