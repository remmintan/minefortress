package org.minefortress.fortress.resources.craft;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.minefortress.interfaces.FortressMinecraftClient;

public class FortressCraftingScreen extends HandledScreen<FortressCraftingScreenHandler>
implements RecipeBookProvider
{

    private static final Identifier TEXTURE = new Identifier("textures/gui/container/crafting_table.png");
    private static final Identifier SCROLLBAR_TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private boolean narrow;

    private final FortressRecipeBookWidget recipeBook = new FortressRecipeBookWidget(RecipeType.CRAFTING);

    private boolean scrolling = false;

    public FortressCraftingScreen(FortressCraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.narrow = this.width < 379;

        this.recipeBook.initialize(this.width, this.height, this.client, this.narrow, this.handler);
        this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
        this.addSelectableChild(this.recipeBook);
        this.setInitialFocus(this.recipeBook);
        this.titleX = 29;
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if(hasCraftsmanInVillage())
            this.recipeBook.update();
        else {
            this.onClose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            final var bounds = getScrollbarBounds();
            if(mouseX >= bounds.x1() && mouseX <= bounds.x2() && mouseY >= bounds.y1() && mouseY <= bounds.y2()) {
                this.scrolling = this.hasScrollbar();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        final var slotAt = super.getSlotAt(mouseX, mouseY);
        if(slotAt != null) return false;
        return super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            final var bounds = getScrollbarBounds();
            this.scrollPosition = ((float)mouseY - (float)bounds.y1() - 7.5f) / ((float)(bounds.y2() - bounds.y1()) - 15.0f);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
            this.handler.scrollItems(this.scrollPosition);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0) this.scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        if (this.recipeBook.isOpen() && this.narrow) {
            this.drawBackground(matrices, delta, mouseX, mouseY);
            this.recipeBook.render(matrices, mouseX, mouseY, delta);
        } else {
            this.recipeBook.render(matrices, mouseX, mouseY, delta);
            super.render(matrices, mouseX, mouseY, delta);
            this.recipeBook.drawGhostSlots(matrices, this.x, this.y, true, delta);
        }
        super.render(matrices, mouseX, mouseY, delta);
        renderScrollbar(matrices);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        this.recipeBook.drawTooltip(matrices, this.x, this.y, mouseX, mouseY);
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

    private float scrollPosition = 0f;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.hasScrollbar()) {
            return false;
        }
        int i = this.handler.getRowsCount() - 4;
        this.scrollPosition = (float)((double)this.scrollPosition - amount / (double)i);
        this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
        this.handler.scrollItems(this.scrollPosition);
        return true;
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

    private void renderScrollbar(MatrixStack matrices) {
        final var bounds = getScrollbarBounds();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SCROLLBAR_TEXTURE);
        this.drawTexture(matrices, bounds.x1(), bounds.y1() + (int)((float)(bounds.y2() - bounds.y1() - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
    }

    private ScrollbarBounds getScrollbarBounds() {
        var x1 = this.x + 177;
        var y1 = this.y + 2;
        var x2 = x1 + 12;
        var y2 = y1 + this.backgroundHeight - 4;
        return new ScrollbarBounds(x1, y1, x2, y2);
    }

    private boolean hasScrollbar() {
        return handler.getRowsCount() > 4;
    }

    private boolean hasCraftsmanInVillage() {
        final var fortressClient = getClient();
        final var clientManager = fortressClient.getFortressClientManager();
        return fortressClient.isFortressGamemode() && clientManager.getProfessionManager().hasProfession("crafter");
    }

    private FortressMinecraftClient getClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
    }

    private static record ScrollbarBounds(int x1, int y1, int x2, int y2) {}
}
