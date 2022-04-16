package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.tasks.ClientTasksHolder;

public class FortressSelectionVisibilityButtonWidget extends FortressItemButtonWidget{

    private static final ItemStack HIDDEN_ITEM_STACK = new ItemStack(Items.ENDER_PEARL);

    public FortressSelectionVisibilityButtonWidget(int x, int y, TooltipSupplier tooltipSupplier, ItemRenderer itemRenderer) {
        super(
                x,
                y,
                Items.ENDER_EYE,
                itemRenderer,
                (btn) -> {
                    final FortressClientWorld fortressWorld = (FortressClientWorld) MinecraftClient.getInstance().world;
                    if(fortressWorld == null) return;
                    final ClientTasksHolder clientTasksHolder = fortressWorld.getClientTasksHolder();
                    clientTasksHolder.toggleSelectionVisibility();
                },
                tooltipSupplier,
                Text.of("")
        );
    }

    @Override
    protected void renderItem(MatrixStack matrices) {
        final FortressClientWorld fortressWorld = (FortressClientWorld) MinecraftClient.getInstance().world;
        if(fortressWorld == null) return;
        final ClientTasksHolder clientTasksHolder = fortressWorld.getClientTasksHolder();
        if(clientTasksHolder.isSelectionHidden()) {
            itemRenderer.renderInGui(HIDDEN_ITEM_STACK, x+2, y+2);
        } else {
            itemRenderer.renderInGui(itemStack, x+2, y+2);
        }
    }

}
