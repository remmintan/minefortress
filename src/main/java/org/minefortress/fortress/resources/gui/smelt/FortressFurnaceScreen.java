package org.minefortress.fortress.resources.gui.smelt;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.recipebook.FurnaceRecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.minefortress.fortress.resources.gui.AbstractFortressRecipeScreen;

public class FortressFurnaceScreen extends AbstractFortressRecipeScreen<FortressFurnaceScreenHandler> {

    private static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/container/furnace.png");
    private final FurnaceRecipeBookScreen furnaceRecipeBookScreen = new FurnaceRecipeBookScreen();

    public FortressFurnaceScreen(FortressFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return furnaceRecipeBookScreen;
    }

    @Override
    protected boolean professionRequirementSatisfied() {
        return true;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        int k;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int i = this.x;
        int j = this.y;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        if (this.handler.isBurning()) {
            k = this.handler.getFuelProgress();
            this.drawTexture(matrices, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }
        k = this.handler .getCookProgress();
        this.drawTexture(matrices, i + 79, j + 34, 176, 14, k + 1, 16);
    }
}
