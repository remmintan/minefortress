package org.minefortress.renderer.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FortressItemButtonWidget extends TexturedButtonWidget {

    private static final Identifier FORTRESS_BUTTON_TEXTURE = new Identifier("minefortress","textures/gui/button.png");
    private static final Identifier ARROWS_TEXTURE = new Identifier("textures/gui/recipe_book.png");

    protected final ItemStack itemStack;

    public boolean checked = false;

    public FortressItemButtonWidget(Item item, PressAction clickAction, String tooltipText) {
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
                (button, matrices, mouseX, mouseY) -> super.renderTooltip(matrices, new LiteralText(tooltipText), mouseX, mouseY),
                Text.of("")
        );
        this.itemStack = new ItemStack(item);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        itemRenderer.renderInGui(itemStack, x+2, y+2);


        RenderSystem.setShaderTexture(0, ARROWS_TEXTURE);
        if(this.checked)
            this.drawTexture(matrices, x-15, y+2, 12, 208, 14, 18);

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(!this.isHovered()) return;
        super.onClick(mouseX, mouseY);
    }

}
