package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import org.minefortress.fortress.resources.gui.AbstractFortressRecipeScreenHandler;
import org.minefortress.fortress.resources.server.ServerResourceManager;

import java.util.ArrayList;
import java.util.List;

import static org.minefortress.MineFortressMod.FORTRESS_FURNACE_SCREEN_HANDLER;
import static org.minefortress.fortress.resources.gui.smelt.FortressFurnacePropertyDelegate.TOTAL_FIELDS;



public class FortressFurnaceScreenHandler extends AbstractFortressRecipeScreenHandler<Inventory> implements FuelChecker{

    private final Inventory furnaceInventory;
    private final PropertyDelegate propertyDelegate;
    private final List<PropertyDelegate> otherFurnaces;

    public FortressFurnaceScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, null, new SimpleInventory(3), new ArrayPropertyDelegate(4), new ArrayList<>());
    }

    public FortressFurnaceScreenHandler(int syncId, PlayerInventory inventory, ServerResourceManager resourceManager, Inventory furnace, PropertyDelegate propertyDelegate, List<PropertyDelegate> otherFurnaces) {
        super(FORTRESS_FURNACE_SCREEN_HANDLER, syncId, resourceManager, inventory.player);

        this.furnaceInventory = furnace;
        this.propertyDelegate = propertyDelegate;
        this.otherFurnaces = otherFurnaces;

        this.addSlot(new Slot(furnaceInventory, 0, 56, 17));
        this.addSlot(new FortressFuelSlot(this, furnaceInventory, 1, 56, 53));
        this.addSlot(new FurnaceOutputSlot(inventory.player, furnaceInventory, 2, 116, 35));

        super.createDefaultsScrollableSlots();
        this.addProperties(this.propertyDelegate);
        for (PropertyDelegate delegate : this.otherFurnaces) {
            this.addProperties(delegate);
        }
    }

    @Override
    protected Inventory getInput() {
        return furnaceInventory;
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        if (furnaceInventory instanceof RecipeInputProvider provider) {
            provider.provideRecipeInputs(finder);
        }
    }

    @Override
    public void clearCraftingSlots() {
        this.getSlot(0).setStackNoCallbacks(ItemStack.EMPTY);
        this.getSlot(2).setStackNoCallbacks(ItemStack.EMPTY);
    }

    @Override
    public void setProperty(int id, int value) {
        if(otherFurnaces.size() * TOTAL_FIELDS < id-3) {
            final var newFurnace = new FortressArrayPropertyDelegate(TOTAL_FIELDS);
            this.addProperties(newFurnace);
            this.otherFurnaces.add(newFurnace);
        }
        super.setProperty(id, value);
    }

    public List<FortressFurnacePropertyDelegate> getFurnaces() {
        return this.otherFurnaces.stream().map(delegate -> (FortressFurnacePropertyDelegate) delegate).toList();
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 2;
    }

    @Override
    public int getCraftingWidth() {
        return 1;
    }

    @Override
    public int getCraftingHeight() {
        return 1;
    }

    @Override
    public int getCraftingSlotCount() {
        return 3;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.FURNACE;
    }

    @Override
    public boolean isFuel(ItemStack item) {
        return AbstractFurnaceBlockEntity.canUseAsFuel(item);
    }

    public int getCookProgress() {
        int i = this.propertyDelegate.get(2);
        int j = this.propertyDelegate.get(3);
        if (j == 0 || i == 0) {
            return 0;
        }
        return i * 24 / j;
    }

    public int getFuelProgress() {
        int i = this.propertyDelegate.get(1);
        if (i == 0) {
            i = 200;
        }
        return this.propertyDelegate.get(0) * 13 / i;
    }

    public boolean isBurning() {
        return this.propertyDelegate.get(0) > 0;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 2) {
                if (!this.insertItem(itemStack2, 3, super.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(itemStack2, itemStack);
            } else {
                final var endIndex = this.slots.size();
                if (index == 1 || index == 0 ? !this.insertItem(itemStack2, 3, endIndex, false) : (this.isSmeltable(itemStack2) ? !this.insertItem(itemStack2, 0, 1, false) : (this.isFuel(itemStack2) ? !this.insertItem(itemStack2, 1, 2, false) : (index >= 3 && index < 30 ? !this.insertItem(itemStack2, 30, endIndex, false) : index >= 30 && index < endIndex && !this.insertItem(itemStack2, 3, 30, false))))) {
                    return ItemStack.EMPTY;
                }
            }
            if (itemStack2.isEmpty()) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, itemStack2);
        }
        return itemStack;
    }

    protected boolean isSmeltable(ItemStack itemStack) {
        return this.world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SimpleInventory(itemStack), this.world).isPresent();
    }

    @Override
    protected void returnInputs() {}

    private static class FortressFurnaceOutputSlot extends FortressSlot {

        private final PlayerEntity player;
        private int amount;


        public FortressFurnaceOutputSlot(PlayerEntity player, Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.player = player;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack takeStack(int amount) {
            if (this.hasStack()) {
                this.amount += Math.min(amount, this.getStack().getCount());
            }
            return super.takeStack(amount);
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.onCrafted(stack);
            super.onTakeItem(player, stack);
        }

        @Override
        protected void onCrafted(ItemStack stack, int amount) {
            this.amount += amount;
            this.onCrafted(stack);
        }

        @Override
        protected void onCrafted(ItemStack stack) {
            stack.onCraft(this.player.getWorld(), this.player, this.amount);
            this.amount = 0;
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxItemCount() {
            return Integer.MAX_VALUE;
        }
    }
}
