package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.FurnaceFuelSlot;
import net.minecraft.screen.slot.Slot;

public class FortressFuelSlot extends Slot {

    private final FuelChecker checker;

    public FortressFuelSlot(FuelChecker checker, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.checker = checker;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.checker.isFuel(stack) || FurnaceFuelSlot.isBucket(stack);
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return FurnaceFuelSlot.isBucket(stack) ? 1 : 64;
    }

}
