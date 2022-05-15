package org.minefortress.fortress.resources.craft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
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
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.client.FortressItemStack;
import org.minefortress.fortress.resources.server.ServerResourceManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.interfaces.FortressSimpleInventory;
import org.minefortress.network.ServerboundScrollCurrentScreenPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.interfaces.ScrollableHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.minefortress.MineFortressMod.FORTRESS_CRAFTING_SCREEN_HANDLER;

public class FortressCraftingScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory>  implements ScrollableHandler {

    private final CraftingInventory input = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();

    private final World world;
    private final SimpleInventory screenInventory = new SimpleInventory(999);
    private final VirtualInventory virtualInventory;
    private final ServerResourceManager serverResourceManager;
    private final PlayerEntity player;

    private int clientCurrentRow = 5;

    public FortressCraftingScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, null);
    }

    public FortressCraftingScreenHandler(int syncId, PlayerInventory inventory, ServerResourceManager resourceManager) {
        super(FORTRESS_CRAFTING_SCREEN_HANDLER, syncId);
        this.serverResourceManager = resourceManager;
        this.player = inventory.player;
        this.world = player.world;
        
        this.addSlot(new FortressCraftingResultSlot(player, this.input, this.result, 0, 124, 35));
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 3; ++column) {
                this.addSlot(new FortressSlot(this.input, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + row * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + 27, 8 + column * 18, 142));
        }

        if(this.serverResourceManager != null) {
            this.virtualInventory = new VirtualInventory(this.serverResourceManager.getAllItems());

            int rowsCount = getRowsCount();
            if(rowsCount > 4) {
                for (int row = 0; row < rowsCount-4; row++) {
                    for (int column = 0; column < 9; ++column) {
                        this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + (row+4) * 9, 8 + column * 18, 150 + row * 18));
                    }
                }
            }

            this.scrollItems(0f);
        } else {
            this.virtualInventory = null;
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
            final var stack = slot.getStack();
            ItemStack itemStack2 = new FortressItemStack(stack.getItem(), stack.getCount());
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
        if(player instanceof FortressServerPlayerEntity fortressServerPlayer) {
            final var fortressServerManager = fortressServerPlayer.getFortressServerManager();
            final var serverResourceManager = fortressServerManager.getServerResourceManager();

            new FortressInputSlotFiller(this).returnInputs();

            final var diff = this.virtualInventory.getDiff();
            diff.added.forEach(stack -> serverResourceManager.setItemAmount(stack.item(), stack.amount()));
            diff.updated.forEach(stack -> serverResourceManager.setItemAmount(stack.item(), stack.amount()));
            diff.removed.forEach(item -> serverResourceManager.setItemAmount(item, 0));
        }
    }

    public void scrollItems(float position) {
        if(serverResourceManager == null) {
            final var packet = new ServerboundScrollCurrentScreenPacket(position);
            FortressClientNetworkHelper.send(FortressChannelNames.SCROLL_CURRENT_SCREEN, packet);
            return;
        }

        final var totalRows = getRowsCount();
        int totalAdditionalRows = totalRows - 4;
        int rowOffset = (int)((double)(position * (float)totalAdditionalRows) + 0.5);
        if (rowOffset < 0) {
            rowOffset = 0;
        }
        for (int row = 0; row < totalRows; ++row) {
            for (int column = 0; column < 9; ++column) {
                var currentRow = row + rowOffset;
                if (currentRow >= totalRows) {
                    currentRow -= totalRows;
                }
                int m = column + currentRow * 9;
                if (m >= 0) {
                    screenInventory.setStack(column + row * 9, virtualInventory.get(m));
                    continue;
                }
                screenInventory.setStack(column + row * 9, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        while (slots.size() < stacks.size()) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + clientCurrentRow * 9, 8 + column * 18, 80 + clientCurrentRow * 18));
            }
            clientCurrentRow++;
        }
        super.updateSlotStacks(revision, stacks, cursorStack);
    }

    public int getRowsCount() {
        return ((serverResourceManager!=null? virtualInventory.size(): slots.size()) + 9 - 1) / 9;
    }

    @Override
    public void fillInputSlots(boolean craftAll, Recipe<?> recipe, ServerPlayerEntity player) {
        new FortressInputSlotFiller(this).fillInputSlots(player, (Recipe<CraftingInventory>) recipe, craftAll);
    }

    public SimpleInventory getScreenInventory() {
        return screenInventory;
    }

    private static class FortressSlot extends Slot {

        public FortressSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
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

    private final class FortressNotInsertableSlot extends FortressSlot {

        public FortressNotInsertableSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public void setStack(ItemStack stack) {
            if(FortressCraftingScreenHandler.this.virtualInventory != null)
                FortressCraftingScreenHandler.this.virtualInventory.set(this.getIndex(), stack);
            super.setStack(stack);
        }

        @Override
        public ItemStack insertStack(ItemStack stack, int count) {
            if (stack.isEmpty() || !this.canInsert(stack)) {
                return stack;
            }
            ItemStack itemStack = this.getStack();
            if (itemStack.isEmpty()) {
                if(this.inventory instanceof FortressSimpleInventory fortressSimpleInventory) {
                    final var i = fortressSimpleInventory.indexOf(stack);
                    if(i != -1) {
                        this.inventory.getStack(i).increment(count);
                        stack.decrement(count);
                    } else {
                        final var split = stack.split(count);
                        this.setStack(split);
                    }
                }
            } else if (ItemStack.canCombine(itemStack, stack)) {
                itemStack.increment(count);
                this.setStack(itemStack);
                stack.decrement(count);
            }
            return stack;
        }
    }

    private static final class FortressCraftingResultSlot extends CraftingResultSlot {

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

    private static final class VirtualInventory {

        private final Set<Item> itemsBefore;
        private final List<ItemStack> items;

        private int rowsOffset = 0;

        VirtualInventory(List<ItemStack> items) {
            this.items = new ArrayList<>(items.stream().filter(it -> !it.isEmpty()).toList());
            this.itemsBefore = this.items
                    .stream()
                    .map(ItemStack::getItem)
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
        }

        ItemStack get(int index) {
            return this.items.get(index);
        }

        void set(int index, ItemStack stack) {
            final var realIndex = index + rowsOffset * 9;
            final var itemsCount = this.items.size();

            final var insertIndex = realIndex < itemsCount ? realIndex : (realIndex - itemsCount);

            if(this.items.get(insertIndex).isEmpty())
                this.items.set(index, stack);
            else
                this.items.add(stack);
        }

        void setRowsOffset(int rowsOffset) {
            this.rowsOffset = rowsOffset;
        }

        int size() {
            return this.items.size();
        }

        InventoryDiff getDiff() {
            final var itemsAfter = this.items.stream()
                    .filter(it -> !it.isEmpty())
                    .map(ItemStack::getItem)
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

            final var itemsToRemove = itemsBefore.stream()
                    .filter(it -> !itemsAfter.contains(it))
                    .toList();

            final var itemsToAdd = items.stream()
                    .filter(it -> !it.isEmpty())
                    .filter(it -> !itemsBefore.contains(it.getItem()))
                    .map(it -> new ItemInfo(it.getItem(), it.getCount()))
                    .toList();

            final var itemsToUpdate = items.stream()
                    .filter(it -> !it.isEmpty())
                    .filter(it -> itemsBefore.contains(it.getItem()))
                    .map(it -> new ItemInfo(it.getItem(), it.getCount()))
                    .toList();

            return new InventoryDiff(itemsToAdd, itemsToUpdate, itemsToRemove);
        }

    }

    private static record InventoryDiff(List<ItemInfo> added, List<ItemInfo> updated, List<Item> removed) {}
}
