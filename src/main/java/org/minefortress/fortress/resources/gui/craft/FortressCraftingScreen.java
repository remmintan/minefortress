package org.minefortress.fortress.resources.gui.craft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import org.minefortress.fortress.resources.gui.AbstractFortressRecipeScreen;
import org.minefortress.fortress.resources.gui.FortressRecipeBookWidget;
import net.remmintan.mods.minefortress.core.interfaces.client.IHoveredBlockProvider;
import org.minefortress.interfaces.IFortressMinecraftClient;

public class FortressCraftingScreen extends AbstractFortressRecipeScreen<FortressCraftingScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/crafting_table.png");
    private final FortressRecipeBookWidget recipeBook = new FortressRecipeBookWidget(RecipeType.CRAFTING);

    public FortressCraftingScreen(FortressCraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float delta, int mouseX, int mouseY) {
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        drawContext.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return recipeBook;
    }

    @Override
    protected boolean professionRequirementSatisfied() {
        final var fortressClient = clientManagersProvider();
        final var clientManager = fortressClient.get_ClientFortressManager();
        return fortressMinecraftClient().is_FortressGamemode() && clientManager.getProfessionManager().hasProfession("crafter");
    }

    private IHoveredBlockProvider hoveredBlockProvider() {
        return (IHoveredBlockProvider) MinecraftClient.getInstance();
    }

    private IClientManagersProvider clientManagersProvider() {
        return (IClientManagersProvider) MinecraftClient.getInstance();
    }

    private IFortressMinecraftClient fortressMinecraftClient() {
        return (IFortressMinecraftClient) MinecraftClient.getInstance();
    }

}
