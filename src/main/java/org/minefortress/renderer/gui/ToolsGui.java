package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import org.minefortress.renderer.gui.widget.FortressItemButtonWidget;

public class ToolsGui extends FortressGuiScreen {

    private final FortressItemButtonWidget selectionType;
    private final ButtonWidget questionButton;

    protected ToolsGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
        this.selectionType = new FortressItemButtonWidget(0, 0, Items.DIAMOND_PICKAXE, itemRenderer, btn -> {
            btn.active = !btn.active;
        });
        this.questionButton = new ButtonWidget(0, 0, 20, 20, new LiteralText("?"), btn -> {
            this.client.setScreen(new BookScreen(new FortressBookContents("Test")));
        });
    }

    @Override
    void tick() {
    }

    @Override
    void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        this.selectionType.setPos(screenWidth - 25, 5);
        this.selectionType.render(p, (int)mouseX, (int)mouseY, 0);
        this.questionButton.x = screenWidth - 25;
        this.questionButton.y = 35;
        this.questionButton.render(p, (int)mouseX, (int)mouseY, 0);
    }

    @Override
    boolean isHovered() {
        return this.selectionType.isHovered() || this.questionButton.isHovered();
    }

    @Override
    void onClick(double mouseX, double mouseY) {
        this.selectionType.onClick(mouseX, mouseY);
        if(questionButton.isHovered())
            this.questionButton.onClick(mouseX, mouseY);
    }
}
