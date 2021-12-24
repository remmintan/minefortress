package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import org.minefortress.renderer.gui.widget.FortressItemButtonWidget;

public class ToolsGui extends FortressGuiScreen {

    private final FortressItemButtonWidget itemButton;

    protected ToolsGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
        this.itemButton = new FortressItemButtonWidget(0, 0, Items.DIAMOND_PICKAXE, itemRenderer, btn -> {
            btn.active = !btn.active;
        });
    }

    @Override
    void tick() {
    }

    @Override
    void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        this.itemButton.setPos(screenWidth - 25, 5);
        this.itemButton.render(p, (int)mouseX, (int)mouseY, 0);
    }

    @Override
    boolean isHovered() {
        return this.itemButton.isHovered();
    }

    @Override
    void onClick(double mouseX, double mouseY) {
        this.itemButton.onClick(mouseX, mouseY);
    }
}
