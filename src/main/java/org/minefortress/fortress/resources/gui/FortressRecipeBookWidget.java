package org.minefortress.fortress.resources.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandler;
import org.minefortress.interfaces.FortressSimpleInventory;

public class FortressRecipeBookWidget extends RecipeBookWidget {

    private int cachedInvChangeCount;
    private final RecipeType recipeType;

    public FortressRecipeBookWidget(RecipeType<? extends Recipe<? extends Inventory>> recipeType) {
        this.recipeType = recipeType;
    }

    @Override
    public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow, AbstractRecipeScreenHandler<?> handler) {
        this.client = client;
        this.craftingScreenHandler = handler;
        client.player.currentScreenHandler = handler;
        this.recipeBook = client.player.getRecipeBook();

        final var craftingRecipes = client.world.getRecipeManager().listAllOfType(recipeType);
        craftingRecipes
                .forEach(res -> {
                    this.recipeBook.add((RecipeEntry<?>)  res);
                    this.recipeBook.display((RecipeEntry<?>) res);
                });

        this.recipeBook = new ClientRecipeBook();
        this.recipeBook.setGuiOpen(this.craftingScreenHandler.getCategory(), true);
        this.recipeBook.reload(craftingRecipes, client.world.getRegistryManager());
        super.initialize(parentWidth, parentHeight, client, narrow, handler);
        this.setOpen(true);
    }

    public void update() {
        super.update();
        if(this.craftingScreenHandler instanceof FortressCraftingScreenHandler fortressHandler) {
            final var screenInventory = (FortressSimpleInventory) fortressHandler.getScreenInventory();
            final var changeCount = screenInventory.get_ChangeCount();
            if (this.cachedInvChangeCount != changeCount) {
                this.recipeFinder.clear();
                screenInventory.populate_RecipeFinder(this.recipeFinder);
                this.craftingScreenHandler.populateRecipeFinder(this.recipeFinder);
                this.refreshResults(false);
                this.cachedInvChangeCount = changeCount;
            }
        }
    }
}
