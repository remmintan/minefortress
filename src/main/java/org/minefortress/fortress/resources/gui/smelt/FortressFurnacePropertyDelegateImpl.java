package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.screen.PropertyDelegate;

public class FortressFurnacePropertyDelegateImpl implements FortressFurnacePropertyDelegate {

    private final PropertyDelegate furnace;
    private final int x;
    private final int y;
    private final int z;
    private final boolean selected;

    public FortressFurnacePropertyDelegateImpl(FurnaceBlockEntity furnaceBlockEntity, boolean selected) {
        this.furnace = furnaceBlockEntity.propertyDelegate;
        this.x = furnaceBlockEntity.getPos().getX();
        this.y = furnaceBlockEntity.getPos().getY();
        this.z = furnaceBlockEntity.getPos().getZ();
        this.selected = selected;
    }

    @Override
    public int get(int index) {
        if (index < furnace.size()) {
            return furnace.get(index);
        }

        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            case 3 -> selected ? 1 : 0;
            default -> throw new IllegalArgumentException("Can't get property " + index + " in  FortressFurnacePropertyDelegateImpl");
        };
    }

    @Override
    public void set(int index, int value) {
        if(index < furnace.size()) {
            furnace.set(index, value);
        }

        throw new IllegalArgumentException("Can't set property " + index + " in  FortressFurnacePropertyDelegateImpl");
    }

    @Override
    public int size() {
        return furnace.size() + ADDITIONAL_FIELDS;
    }
}
