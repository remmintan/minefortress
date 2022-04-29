package org.minefortress.fortress.resources.craft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.minefortress.fortress.resources.server.ServerResourceManager;

import static org.minefortress.MineFortressMod.FORTRESS_CRAFTING_SCREEN_HANDLER;

public class FortressCraftingScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory> {

    private final CraftingInventory input = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();

    private final World world;
    private final SimpleInventory screenInventory = new SimpleInventory(36);;
    private final ServerResourceManager serverResourceManager;
    private final PlayerEntity player;

    public FortressCraftingScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, null);
    }

    public FortressCraftingScreenHandler(int syncId, PlayerInventory inventory, ServerResourceManager resourceManager) {
        super(FORTRESS_CRAFTING_SCREEN_HANDLER, syncId);
        this.serverResourceManager = resourceManager;
        this.player = inventory.player;
        this.world = player.world;
        
        this.addSlot(new CraftingResultSlot(player, this.input, this.result, 0, 124, 35));
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 3; ++column) {
                this.addSlot(new Slot(this.input, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(this.screenInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(this.screenInventory, column, 8 + column * 18, 142));
        }

        if(this.serverResourceManager != null) {
            this.scrollItems(0f);
        }
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.input.clear();
        this.result.clear();
    }

    @Override
    public boolean matches(Recipe<? super CraftingInventory> recipe) {
        return recipe.matches(this.input, world);
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return this.input.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.input.getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 10;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 0) {
                if (!this.insertItem(itemStack2, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(itemStack2, itemStack);
            } else if (index >= 10 && index < 46 ? !this.insertItem(itemStack2, 1, 10, false) && (index < 37 ? !this.insertItem(itemStack2, 37, 46, false) : !this.insertItem(itemStack2, 10, 37, false)) : !this.insertItem(itemStack2, 10, 46, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, itemStack2);
            if (index == 0) {
                player.dropItem(itemStack2, false);
            }
        }
        return itemStack;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        CraftingScreenHandler.updateResult(this, world, this.player, this.input, this.result);
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
    }

    public void scrollItems(float position) {
        final var items = this.serverResourceManager.getAllItems();
        int i = (items.size() + 9 - 1) / 9 - 4;
        int j = (int)((double)(position * (float)i) + 0.5);
        if (j < 0) {
            j = 0;
        }
        for (int k = 0; k < 4; ++k) {
            for (int l = 0; l < 9; ++l) {
                int m = l + (k + j) * 9;
                if (m >= 0 && m < items.size()) {
                    screenInventory.setStack(l + k * 9, items.get(m));
                    continue;
                }
                screenInventory.setStack(l + k * 9, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void fillInputSlots(boolean craftAll, Recipe<?> recipe, ServerPlayerEntity player) {
        new FortressInputSlotFiller(this).fillInputSlots(player, (Recipe<CraftingInventory>) recipe, craftAll);
    }

    public SimpleInventory getScreenInventory() {
        return screenInventory;
    }
}
