package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.screen.PropertyDelegate;

public interface FortressFurnacePropertyDelegate extends PropertyDelegate {

    int ADDITIONAL_FIELDS = 4;
    int TOTAL_FIELDS = ADDITIONAL_FIELDS + 4;

    default int getBurnTime() {
        return this.get(0);
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

    default boolean isSelected() {
        return this.get(7)==1;
    }

    default int getCookProgress() {
        int i = this.get(2);
        int j = this.get(3);
        if (j == 0 || i == 0) {
            return 0;
        }
        return i * 24 / j;
    }


}
