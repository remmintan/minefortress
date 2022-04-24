package org.minefortress.renderer.gui.resources;

import net.minecraft.inventory.SimpleInventory;

public class FortressSimpleInventory extends SimpleInventory {

    public FortressSimpleInventory(int size) {
        super(size);
    }

    @Override
    public int getMaxCountPerStack() {
        return 10000;
    }
}
