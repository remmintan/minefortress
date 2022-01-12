package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.minefortress.interfaces.FortressMinecraftClient;

public class FortressSelectionVisibilityButtonWidget extends FortressItemButtonWidget{

    private static final ItemStack HIDDEN_ITEM_STACK = new ItemStack(Items.ENDER_PEARL);

    public FortressSelectionVisibilityButtonWidget(int x, int y, TooltipSupplier tooltipSupplier, ItemRenderer itemRenderer) {
        super(
                x,
                y,
                Items.ENDER_EYE,
                itemRenderer,
                (btn) -> {
                    final MinecraftClient instance = MinecraftClient.getInstance();
                    final FortressMinecraftClient fortressClient = (FortressMinecraftClient) instance;
                    fortressClient.getSelectionManager().toggleSelectionVisibility();
                },
                tooltipSupplier,
                Text.of("")
        );

    }

    @Override
    protected void renderItem(MatrixStack matrices) {
        final MinecraftClient instance = MinecraftClient.getInstance();
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) instance;
        if(fortressClient.getSelectionManager().isSelectionHidden()) {
            itemRenderer.renderInGui(HIDDEN_ITEM_STACK, x+2, y+2);
        } else {
            itemRenderer.renderInGui(itemStack, x+2, y+2);
        }
    }

}
