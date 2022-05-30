package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.screen.PropertyDelegate;

public interface FortressFurnacePropertyDelegate extends PropertyDelegate {

    int ADDITIONAL_FIELDS = 4;
    int TOTAL_FIELDS = ADDITIONAL_FIELDS + 4;

    default int getBurnTime() {
        return this.get(0);
    }

    default int getFuelTime() {
        return this.get(1);
    }

    default int getCookTime() {
        return this.get(2);
    }

    default int getCookTimeTotal() {
        return this.get(3);
    }

    default int getPosX() {
        return this.get(4);
    }

    default int getPosY() {
        return this.get(5);
    }

    default int getPosZ() {
        return this.get(6);
    }

    default int isSelected() {
        return this.get(7);
    }

}
