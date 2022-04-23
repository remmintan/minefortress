package org.minefortress.renderer.gui.resources;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class FortressSurvivalInventoryScreenHandler extends CreativeInventoryScreen.CreativeScreenHandler {

    private static final String CUSTOM_FORTRESS_SURVIVAL_LOCK_KEY = "CustomCreativeLock";
    private final Inventory INVENTORY  = new SimpleInventory(45);

    private final ScreenHandler parent;

    public FortressSurvivalInventoryScreenHandler(PlayerEntity player) {
        super(player);
        super.slots.clear();
        super.itemList.clear();
        INVENTORY.clear();

        int i;
        this.parent = player.playerScreenHandler;
        PlayerInventory playerInventory = player.getInventory();

        for (i = 0; i < 5; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new LockableSlot(INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 9 + i * 18, 112));
        }

        this.scrollItems(0.0f);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void scrollItems(float position) {
        if(itemList == null || INVENTORY == null) return;

        int i = (this.itemList.size() + 9 - 1) / 9 - 5;
        int j = (int)((double)(position * (float)i) + 0.5);
        if (j < 0) {
            j = 0;
        }
        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 9; ++l) {
                int m = l + (k + j) * 9;
                if (m >= 0 && m < this.itemList.size()) {
                    INVENTORY.setStack(l + k * 9, this.itemList.get(m));
                    continue;
                }
                INVENTORY.setStack(l + k * 9, ItemStack.EMPTY);
            }
        }
    }

    public boolean shouldShowScrollbar() {
        return this.itemList.size() > 45;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot slot;
        if (index >= this.slots.size() - 9 && index < this.slots.size() && (slot = this.slots.get(index)) != null && slot.hasStack()) {
            slot.setStack(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != INVENTORY;
    }

    @Override
    public boolean canInsertIntoSlot(Slot slot) {
        return slot.inventory != INVENTORY;
    }

    @Override
    public ItemStack getCursorStack() {
        return this.parent.getCursorStack();
    }

    @Override
    public void setCursorStack(ItemStack stack) {
        this.parent.setCursorStack(stack);
    }

    static class LockableSlot extends Slot {
        public LockableSlot(Inventory inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
//            return false;
            if (super.canTakeItems(playerEntity) && this.hasStack()) {
                return this.getStack().getSubNbt(CUSTOM_FORTRESS_SURVIVAL_LOCK_KEY) == null;
            }
            return !this.hasStack();
        }
    }

}
