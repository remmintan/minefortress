package org.minefortress.renderer.gui.widget;

import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class FortressItemButtonWidget extends TexturedButtonWidget {

    private static final Identifier FORTRESS_BUTTON_TEXTURE = new Identifier("minefortress","textures/gui/button.png");

    private final ItemStack stack;
    private final ItemRenderer itemRenderer;

    public FortressItemButtonWidget(int x, int y, Item item, ItemRenderer itemRenderer, PressAction clickAction) {
        super(x, y, 20, 20, 0, 0, 20, FORTRESS_BUTTON_TEXTURE, 32, 64, clickAction);
        this.stack = new ItemStack(item);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        itemRenderer.renderInGui(stack, x+1, y+1);
    }
}
