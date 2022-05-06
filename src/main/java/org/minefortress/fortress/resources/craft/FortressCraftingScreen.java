package org.minefortress.fortress.resources.craft;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.professions.ProfessionsScreen;

public class FortressCraftingScreen extends HandledScreen<FortressCraftingScreenHandler>
implements RecipeBookProvider
{

    private static final Identifier TEXTURE = new Identifier("textures/gui/container/crafting_table.png");
    private boolean narrow;

    private final FortressRecipeBookWidget recipeBook = new FortressRecipeBookWidget();
    private FortressClientManager getManager;

    public FortressCraftingScreen(FortressCraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.narrow = this.width < 379;

        if(hasCraftsmanInVillage()) {
            this.recipeBook.initialize(this.width, this.height, this.client, this.narrow, this.handler);
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            this.addSelectableChild(this.recipeBook);
            this.setInitialFocus(this.recipeBook);
            this.titleX = 29;
        } else {
            this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 2 + 24 - 16, 204, 20, new LiteralText("Back"), button -> {
                this.client.setScreen(null);
            }));
            this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 2 + 48 - 16, 204, 20, new LiteralText("To professions menu"), button -> {
                this.client.setScreen(new ProfessionsScreen(getClient()));
            }));
        }
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        this.recipeBook.update();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        if(hasCraftsmanInVillage()) {
            if (this.recipeBook.isOpen() && this.narrow) {
                this.drawBackground(matrices, delta, mouseX, mouseY);
                this.recipeBook.render(matrices, mouseX, mouseY, delta);
            } else {
                this.recipeBook.render(matrices, mouseX, mouseY, delta);
                super.render(matrices, mouseX, mouseY, delta);
                this.recipeBook.drawGhostSlots(matrices, this.x, this.y, true, delta);
            }
            this.drawMouseoverTooltip(matrices, mouseX, mouseY);
            this.recipeBook.drawTooltip(matrices, this.x, this.y, mouseX, mouseY);
        } else {
            FortressCraftingScreen.drawCenteredText(matrices, this.textRenderer, "You need at least one Craftsman in your village. Go to professions menu and hire one", this.width / 2, this.height / 2, 0xFFFFFF);
        }
        super.render(matrices, mouseX, mouseY, delta);
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
    public void removed() {
        this.recipeBook.close();
        super.removed();
    }

    @Override
    public void refreshRecipeBook() {
        recipeBook.refresh();
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return recipeBook;
    }

    private boolean hasCraftsmanInVillage() {
        final var fortressClient = getClient();
        getManager = fortressClient.getFortressClientManager();
        final var clientManager = getManager;
        return fortressClient.isFortressGamemode() && clientManager.getProfessionManager().hasProfession("crafter");
    }

    private FortressMinecraftClient getClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
    }
}