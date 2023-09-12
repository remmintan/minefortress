package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.minefortress.renderer.gui.hud.interfaces.IHudButton;
import org.minefortress.renderer.gui.hud.interfaces.IItemHudElement;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ItemButtonWidget extends TexturedButtonWidget implements IHudButton, IItemHudElement {

    protected static final Identifier FORTRESS_BUTTON_TEXTURE = new Identifier("minefortress","textures/gui/button.png");
    protected static final int FORTRESS_BUTTON_HEIGHT = 64;
    protected static final int FORTRESS_BUTTON_WIDTH = 32;
    private static final Identifier ARROWS_TEXTURE = new Identifier("textures/gui/recipe_book.png");

    protected final ItemStack itemStack;
    private final int anchorX;
    private final int anchorY;

    public boolean checked = false;
    private final Function<ItemButtonWidget, Optional<String>> tooltipTextProvider;
    public ItemButtonWidget(int anchorX, int anchorY, Item item, PressAction clickAction, String tooltipText) {
        super(
                0,
                0,
                20,
                20,
                0,
                0,
                20,
                FORTRESS_BUTTON_TEXTURE,
                FORTRESS_BUTTON_WIDTH,
                FORTRESS_BUTTON_HEIGHT,
                clickAction
        );
        this.setTooltip(Tooltip.of(Text.literal(tooltipText)));
        this.itemStack = new ItemStack(item);
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.tooltipTextProvider = null;
    }

    public ItemButtonWidget(int anchorX, int anchorY, Item item, PressAction clickAction, Function<ItemButtonWidget, Optional<String>> optTooltip) {
        super(
                0,
                0,
                20,
                20,
                0,
                0,
                20,
                FORTRESS_BUTTON_TEXTURE,
                FORTRESS_BUTTON_WIDTH,
                FORTRESS_BUTTON_HEIGHT,
                clickAction
        );
        this.itemStack = new ItemStack(item);
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.tooltipTextProvider = optTooltip;
    }

    @Override
    public final void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
        if(!this.visible)return;
        renderItem(drawContext);

        if(this.checked){
            this.drawTexture(drawContext, ARROWS_TEXTURE, this.getX()-15, this.getY()+2, 12, 208, 0, 14, 18, 512, 512);
        }

        if(this.hovered) {
            final var client = MinecraftClient.getInstance();
            final var textRenderer = client.textRenderer;
            final var tooltip = constructTooltip();
            if (tooltip != null) {
                final List<OrderedText> lines = tooltip.getLines(client);
                drawContext.drawTooltip(textRenderer, lines, this.getTooltipPositioner(), mouseX, mouseY);
            }
        }
    }

    @Nullable
    private Tooltip constructTooltip() {
        if(tooltipTextProvider != null) {
            final var tooltipText = tooltipTextProvider.apply(this);
            if(tooltipText.isPresent()) {
                return Tooltip.of(Text.of(tooltipText.get()));
            }
        } else {
            return this.getTooltip();
        }
        return null;
    }

    protected void renderItem(DrawContext drawContext) {
        renderBareItem(drawContext);
    }

    protected final void renderBareItem(DrawContext drawContext) {
        drawContext.drawItem(itemStack, this.getX()+2, this.getY()+2);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(!this.isSelected()) return;
        super.onClick(mouseX, mouseY);
    }

    @Override
    public int getAnchorX() {
        return anchorX;
    }

    @Override
    public int getAnchorY() {
        return anchorY;
    }

    @Override
    public boolean isSelected() {
        return super.isSelected();
    }

    @Override
    public void setPos(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    public boolean isHovered() {
        return super.isHovered();
    }
}
