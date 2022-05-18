package org.minefortress.fortress.resources.gui;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fortress.resources.gui.AbstractFortressRecipeScreenHandler;
import org.minefortress.interfaces.FortressSimpleInventory;

import java.util.ArrayList;
import java.util.Iterator;

public final class FortressInputSlotFiller implements RecipeGridAligner<Integer> {
    private final RecipeMatcher matcher = new RecipeMatcher();
    private SimpleInventory inventory;
    private final AbstractFortressRecipeScreenHandler handler;

    public FortressInputSlotFiller(AbstractFortressRecipeScreenHandler handler) {
        this.handler = handler;
    }

    public void fillInputSlots(ServerPlayerEntity entity, @Nullable Recipe<CraftingInventory> recipe, boolean craftAll) {
        if (recipe == null) {
            return;
        }

        this.inventory = handler.getScreenInventory();

        if (!this.canReturnInputs() && !entity.isCreative()) {
            return;
        }
        this.matcher.clear();
        ((FortressSimpleInventory)handler.getScreenInventory()).populateRecipeFinder(this.matcher);
        this.handler.populateRecipeFinder(this.matcher);
        if (this.matcher.match(recipe, null)) {
            this.fillInputSlots(recipe, craftAll);
        } else {
            this.returnInputs();
            entity.networkHandler.sendPacket(new CraftFailedResponseS2CPacket(entity.currentScreenHandler.syncId, recipe));
        }
        handler.getScreenInventory().markDirty();
    }

    public void returnInputs() {
        if(this.inventory == null)
            this.inventory = handler.getScreenInventory();
        for (int i = 0; i < this.handler.getCraftingSlotCount(); ++i) {
            if (!this.handler.canInsertIntoSlot(i)) continue;
            ItemStack itemStack = this.handler.getSlot(i).getStack().copy();
            this.inventory.addStack(itemStack);
            this.handler.getSlot(i).setStack(itemStack);
        }
        this.handler.clearCraftingSlots();
    }

    private void fillInputSlots(Recipe<CraftingInventory> recipe, boolean craftAll) {
        Object itemStack;
        int j;
        boolean bl = this.handler.matches(recipe);
        int i = this.matcher.countCrafts(recipe, null);
        if (bl) {
            for (j = 0; j < this.handler.getCraftingHeight() * this.handler.getCraftingWidth() + 1; ++j) {
                if (j == this.handler.getCraftingResultSlotIndex() || ((ItemStack)(itemStack = this.handler.getSlot(j).getStack())).isEmpty() || Math.min(i, ((ItemStack)itemStack).getMaxCount()) >= ((ItemStack)itemStack).getCount() + 1) continue;
                return;
            }
        }
        if (this.matcher.match(recipe, (IntList)(itemStack = new IntArrayList()), j = this.getAmountToFill(craftAll, i, bl))) {
            int k = j;
            IntListIterator intListIterator = ((IntList)itemStack).iterator();
            while (intListIterator.hasNext()) {
                int l = intListIterator.next();
                int m = RecipeMatcher.getStackFromId(l).getMaxCount();
                if (m >= k) continue;
                k = m;
            }
            j = k;
            if (this.matcher.match(recipe, (IntList)itemStack, j)) {
                this.returnInputs();
                this.alignRecipeToGrid(this.handler.getCraftingWidth(), this.handler.getCraftingHeight(), this.handler.getCraftingResultSlotIndex(), recipe, ((IntList)itemStack).iterator(), j);
            }
        }
    }

    @Override
    public void acceptAlignedInput(Iterator<Integer> inputs, int slot, int amount, int gridX, int gridY) {
        Slot slot2 = this.handler.getSlot(slot);
        ItemStack itemStack = RecipeMatcher.getStackFromId(inputs.next());
        if (!itemStack.isEmpty()) {
            for (int i = 0; i < amount; ++i) {
                this.fillInputSlot(slot2, itemStack);
            }
        }
    }

    private int getAmountToFill(boolean craftAll, int limit, boolean recipeInCraftingSlots) {
        int i = 1;
        if (craftAll) {
            i = limit;
        } else if (recipeInCraftingSlots) {
            i = 64;
            for (int j = 0; j < this.handler.getCraftingWidth() * this.handler.getCraftingHeight() + 1; ++j) {
                ItemStack itemStack;
                if (j == this.handler.getCraftingResultSlotIndex() || (itemStack = this.handler.getSlot(j).getStack()).isEmpty() || i <= itemStack.getCount()) continue;
                i = itemStack.getCount();
            }
            if (i < 64) {
                ++i;
            }
        }
        return i;
    }

    private void fillInputSlot(Slot slot, ItemStack stack) {
        int i = ((FortressSimpleInventory)this.inventory).indexOf(stack);
        if (i == -1) {
            return;
        }
        ItemStack itemStack = this.inventory.getStack(i).copy();
        if (itemStack.isEmpty()) {
            return;
        }
        if (itemStack.getCount() > 1) {
            this.inventory.removeStack(i, 1);
        } else {
            this.inventory.removeStack(i);
        }
        itemStack.setCount(1);
        if (slot.getStack().isEmpty()) {
            slot.setStack(itemStack);
        } else {
            slot.getStack().increment(1);
        }
    }

    private boolean canReturnInputs() {
        ArrayList<ItemStack> list = Lists.newArrayList();
        int i = this.getFreeInventorySlots();
        for (int j = 0; j < this.handler.getCraftingWidth() * this.handler.getCraftingHeight() + 1; ++j) {
            ItemStack itemStack;
            if (j == this.handler.getCraftingResultSlotIndex() || (itemStack = this.handler.getSlot(j).getStack().copy()).isEmpty()) continue;
            int k = ((FortressSimpleInventory)this.inventory).getOccupiedSlotWithRoomForStack(itemStack);
            if (k == -1 && list.size() <= i) {
                for (ItemStack itemStack2 : list) {
                    if (!itemStack2.isItemEqualIgnoreDamage(itemStack) || itemStack2.getCount() == itemStack2.getMaxCount() || itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxCount()) continue;
                    itemStack2.increment(itemStack.getCount());
                    itemStack.setCount(0);
                    break;
                }
                if (itemStack.isEmpty()) continue;
                if (list.size() < i) {
                    list.add(itemStack);
                    continue;
                }
                return false;
            }
            if (k != -1) continue;
            return false;
        }
        return true;
    }

    private int getFreeInventorySlots() {
        int i = 0;
        for (ItemStack itemStack : ((FortressSimpleInventory)this.inventory).getStacks()) {
            if (!itemStack.isEmpty()) continue;
            ++i;
        }
        return i;
    }
}
