package org.minefortress.fortress.resources.gui;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public abstract class AbstractFortressRecipeScreen<T extends AbstractFortressRecipeScreenHandler> extends HandledScreen<T> implements RecipeBookProvider {

    private static final Identifier SCROLLBAR_TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private boolean narrow;
    private float scrollPosition = 0f;
    private boolean scrolling = false;

    public AbstractFortressRecipeScreen(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.narrow = this.width < 379;

        getRecipeBookWidget().initialize(this.width, this.height, this.client, this.narrow, this.handler);
        this.x = getRecipeBookWidget().findLeftEdge(this.width, this.backgroundWidth);
        this.addSelectableChild(getRecipeBookWidget());
        this.setInitialFocus(getRecipeBookWidget());
        this.titleX = 29;
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if(professionRequirementSatisfied())
            getRecipeBookWidget().update();
        else {
            this.close();
        }
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0) this.scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        if (getRecipeBookWidget().isOpen() && this.narrow) {
            this.drawBackground(drawContext, delta, mouseX, mouseY);
            getRecipeBookWidget().render(drawContext, mouseX, mouseY, delta);
        } else {
            getRecipeBookWidget().render(drawContext, mouseX, mouseY, delta);
            super.render(drawContext, mouseX, mouseY, delta);
            getRecipeBookWidget().drawGhostSlots(drawContext, this.x, this.y, true, delta);
        }
        super.render(drawContext, mouseX, mouseY, delta);
        renderScrollbar(drawContext);
        this.drawMouseoverTooltip(drawContext, mouseX, mouseY);
        getRecipeBookWidget().drawTooltip(drawContext, this.x, this.y, mouseX, mouseY);
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
        getRecipeBookWidget().reset();
        super.removed();
    }



    @Override
    public void refreshRecipeBook() {
        getRecipeBookWidget().refresh();
    }

    public abstract RecipeBookWidget getRecipeBookWidget();

    abstract protected boolean professionRequirementSatisfied();

    private void renderScrollbar(DrawContext drawContext) {
        final var bounds = getScrollbarBounds();
        drawContext.drawTexture(SCROLLBAR_TEXTURE, bounds.x1(), bounds.y1() + (int)((float)(bounds.y2() - bounds.y1() - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
    }

    private AbstractFortressRecipeScreen.ScrollbarBounds getScrollbarBounds() {
        var x1 = this.x + 177;
        var y1 = this.y + 2;
        var x2 = x1 + 12;
        var y2 = y1 + this.backgroundHeight - 4;
        return new AbstractFortressRecipeScreen.ScrollbarBounds(x1, y1, x2, y2);
    }

    private boolean hasScrollbar() {
        return handler.getRowsCount() > 4;
    }

    private static record ScrollbarBounds(int x1, int y1, int x2, int y2) {}

}
