package org.minefortress.fortress.resources.craft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.screen.AbstractRecipeScreenHandler;

public class FortressRecipeBookWidget extends RecipeBookWidget {

    @Override
    public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow, AbstractRecipeScreenHandler<?> craftingScreenHandler) {
        super.initialize(parentWidth, parentHeight, client, narrow, craftingScreenHandler);
        this.recipeBook = new ClientRecipeBook();
        this.setOpen(true);
    }
}
