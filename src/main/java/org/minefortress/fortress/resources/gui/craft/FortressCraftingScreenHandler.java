package org.minefortress.fortress.resources.gui.craft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.minefortress.fortress.resources.gui.AbstractFortressRecipeScreenHandler;
import org.minefortress.fortress.resources.server.ServerResourceManager;

import static org.minefortress.MineFortressMod.FORTRESS_CRAFTING_SCREEN_HANDLER;

public class FortressCraftingScreenHandler extends AbstractFortressRecipeScreenHandler<CraftingInventory> {

    private final CraftingInventory input = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();

    public FortressCraftingScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, null);
    }

    public FortressCraftingScreenHandler(int syncId, PlayerInventory inventory, ServerResourceManager resourceManager) {
        super(FORTRESS_CRAFTING_SCREEN_HANDLER, syncId, resourceManager, inventory.player);

        this.addSlot(new FortressCraftingResultSlot(player, getInput(), this.result, 0, 124, 35));
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 3; ++column) {
                this.addSlot(new FortressSlot(getInput(), column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }
        createDefaultsScrollableSlots();
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        CraftingScreenHandler.updateResult(this, world, player, getInput(), getResult());
        super.onContentChanged(inventory);
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        getInput().provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        getInput().clear();
        getResult().clear();
    }

    @Override
    protected CraftingInventory getInput() {
        return this.input;
    }


    private CraftingResultInventory getResult() {
        return this.result;
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return getInput().getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return getInput().getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 10;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    protected static final class FortressCraftingResultSlot extends CraftingResultSlot {

        public FortressCraftingResultSlot(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x, int y) {
            super(player, input, inventory, index, x, y);
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxItemCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public ItemStack insertStack(ItemStack stack) {
            if (stack.isEmpty() || !this.canInsert(stack)) {
                return stack;
            }
            ItemStack itemStack = this.getStack();
            if (itemStack.isEmpty()) {
                this.setStack(stack);
            } else if (ItemStack.canCombine(itemStack, stack)) {
                itemStack.increment(stack.getCount());
                this.setStack(itemStack);
            }
            return stack;
        }
    }


}
