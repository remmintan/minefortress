package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import org.minefortress.fortress.resources.gui.AbstractFortressRecipeScreenHandler;
import org.minefortress.fortress.resources.server.ServerResourceManager;

import static org.minefortress.MineFortressMod.FORTRESS_FURNACE_SCREEN_HANDLER;

public class FortressFurnaceScreenHandler extends AbstractFortressRecipeScreenHandler<Inventory> implements FuelChecker{

    private final Inventory furnaceInventory;
    private final PropertyDelegate propertyDelegate;

    public FortressFurnaceScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, null, new SimpleInventory(3), new ArrayPropertyDelegate(4));
    }

    public FortressFurnaceScreenHandler(int syncId, PlayerInventory inventory, ServerResourceManager resourceManager, Inventory furnace, PropertyDelegate propertyDelegate) {
        super(FORTRESS_FURNACE_SCREEN_HANDLER, syncId, resourceManager, inventory.player);

        this.furnaceInventory = furnace;
        this.propertyDelegate = propertyDelegate;

        this.addSlot(new FortressSlot(furnaceInventory, 0, 56, 17));
        this.addSlot(new FortressFuelSlot(this, furnaceInventory, 1, 56, 53));
        this.addSlot(new FortressFurnaceOutputSlot(inventory.player, furnaceInventory, 2, 116, 35));

        super.createDefaultsScrollableSlots();
        this.addProperties(this.propertyDelegate);
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
        this.getSlot(0).setStack(ItemStack.EMPTY);
        this.getSlot(2).setStack(ItemStack.EMPTY);
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
            stack.onCraft(this.player.world, this.player, this.amount);
            this.amount = 0;
        }

    }
}
