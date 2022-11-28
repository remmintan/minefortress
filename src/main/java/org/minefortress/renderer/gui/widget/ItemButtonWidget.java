package org.minefortress.renderer.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.minefortress.renderer.gui.hud.interfaces.IHudButton;
import org.minefortress.renderer.gui.hud.interfaces.IItemHudElement;
import org.minefortress.renderer.gui.tooltip.BasicTooltipSupplier;
import org.minefortress.renderer.gui.tooltip.OptionalTooltipSupplier;

import java.util.Optional;
import java.util.function.Function;

public class ItemButtonWidget extends TexturedButtonWidget implements IHudButton, IItemHudElement {

    protected static final Identifier FORTRESS_BUTTON_TEXTURE = new Identifier("minefortress","textures/gui/button.png");
    private static final Identifier ARROWS_TEXTURE = new Identifier("textures/gui/recipe_book.png");

    protected final ItemStack itemStack;
    private final int anchorX;
    private final int anchorY;

    public boolean checked = false;

    protected ItemRenderer itemRenderer;

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
                32,
                64,
                clickAction,
                new BasicTooltipSupplier(tooltipText),
                Text.of("")
        );
        this.itemStack = new ItemStack(item);
        this.anchorX = anchorX;
        this.anchorY = anchorY;
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
                32,
                64,
                clickAction,
                new OptionalTooltipSupplier(),
                Text.of("")
        );
        ((OptionalTooltipSupplier)super.tooltipSupplier).provideTooltipText(() -> optTooltip.apply(this));
        this.itemStack = new ItemStack(item);
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    @Override
    public void setItemRenderer(ItemRenderer renderer) {
        this.itemRenderer = renderer;
    }

    @Override
    public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        renderItem(matrices);

        if(this.checked){
            RenderSystem.setShaderTexture(0, ARROWS_TEXTURE);
            this.drawTexture(matrices, x-15, y+2, 12, 208, 14, 18);
        }
    }

    protected void renderItem(MatrixStack m) {
        renderBareItem();
    }

    protected final void renderBareItem() {
        itemRenderer.renderInGui(itemStack, x+2, y+2);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(!this.isHovered()) return;
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
    public boolean isHovered() {
        return super.isHovered();
    }
}
