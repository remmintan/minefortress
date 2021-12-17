package org.minefortress.entity.ai.controls;


import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

abstract class ActionControl {

    protected boolean actionNeeded = false;
    private List<BlockPos> results = new ArrayList<>();

    public void needAction() {
        actionNeeded = true;
    }

    protected abstract BlockPos doAction();

    public void tick() {
        if (actionNeeded) {
            final BlockPos result = doAction();
            if(result != null) {
                actionNeeded = false;
                results.add(result);
            }
        }
    }

    public void clearResults() {
        this.clearResults(results);
        results.clear();
    }

    protected abstract void clearResults(List<BlockPos> results);

}
