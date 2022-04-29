package org.minefortress.fortress.resources.craft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.AbstractRecipeScreenHandler;

import java.util.ArrayList;

public class FortressRecipeBookWidget extends RecipeBookWidget {

    @Override
    public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow, AbstractRecipeScreenHandler<?> craftingScreenHandler) {
        this.client = client;
        this.craftingScreenHandler = craftingScreenHandler;
        client.player.currentScreenHandler = craftingScreenHandler;
        this.recipeBook = client.player.getRecipeBook();
        client.keyboard.setRepeatEvents(true);

        final var craftingRecipes = client.world.getRecipeManager().listAllOfType(RecipeType.CRAFTING);
        craftingRecipes
                .forEach(res -> {
                    this.recipeBook.add(res);
                    this.recipeBook.display(res);
                });

        this.recipeBook = new ClientRecipeBook();
        this.recipeBook.reload(new ArrayList<>(craftingRecipes));
        this.recipeBook.setGuiOpen(this.craftingScreenHandler.getCategory(), true);
        super.initialize(parentWidth, parentHeight, client, narrow, craftingScreenHandler);
        this.setOpen(true);

    }
}
