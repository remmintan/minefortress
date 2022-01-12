package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.minefortress.interfaces.FortressMinecraftClient;

public class FortressBlueprintsButtonWidget extends FortressItemButtonWidget {


    public FortressBlueprintsButtonWidget(int x, int y, Item item, ItemRenderer itemRenderer, PressAction clickAction, TooltipSupplier tooltipSupplier, Text text) {
        super(x, y, item, itemRenderer, clickAction, tooltipSupplier, text);
    }

    @Override
    protected void renderItem(MatrixStack matrices) {
        final MinecraftClient instance = MinecraftClient.getInstance();
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) instance;
        if(fortressClient.getBlueprintManager().hasSelectedBlueprint()) {
            final TextRenderer textRenderer = instance.textRenderer;
            drawCenteredText(matrices, textRenderer, "X", this.x + this.width / 2, this.y + this.height / 4, 0xFFFFFF);
        } else {
            itemRenderer.renderInGui(itemStack, x+2, y+2);
        }
    }
}
