package org.minefortress.fortress.resources.gui.craft;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.minefortress.fortress.resources.gui.AbstractFortressRecipeScreen;
import org.minefortress.interfaces.FortressMinecraftClient;

public class FortressCraftingScreen extends AbstractFortressRecipeScreen<FortressCraftingScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/crafting_table.png");
    private final FortressRecipeBookWidget recipeBook = new FortressRecipeBookWidget(RecipeType.CRAFTING);

    public FortressCraftingScreen(FortressCraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return recipeBook;
    }

    @Override
    protected boolean professionRequirementSatisfied() {
        final var fortressClient = getClient();
        final var clientManager = fortressClient.getFortressClientManager();
        return fortressClient.isFortressGamemode() && clientManager.getProfessionManager().hasProfession("crafter");
    }

    private FortressMinecraftClient getClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
    }
}
