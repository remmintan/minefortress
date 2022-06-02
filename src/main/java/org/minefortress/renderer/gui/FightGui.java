package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.fight.ClientFightSelectionManager;
import org.minefortress.interfaces.FortressMinecraftClient;

class FightGui extends FortressGuiScreen{

    private static final int SELECTION_COLOR = 0x060606;

    private final ClientFightSelectionManager fightSelectionManager;

    FightGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
        if(client instanceof FortressMinecraftClient fortressMinecraftClient) {
            final var fortressManager = fortressMinecraftClient.getFortressClientManager();
            this.fightSelectionManager = fortressManager.getFightManager().getSelectionManager();
        } else {
            throw new IllegalArgumentException("Client is not a FortressClient!");
        }
    }

    @Override
    void render(MatrixStack matrices, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        if(!this.fightSelectionManager.isSelecting()) return;
        final var selectionStartPos = this.fightSelectionManager.getSelectionStartPos();
        final var selectionCurPos = this.fightSelectionManager.getSelectionCurPos();

        super.drawHorizontalLine(matrices, selectionStartPos.getX(), selectionCurPos.getX(), selectionStartPos.getY(), SELECTION_COLOR);
        super.drawVerticalLine(matrices, selectionCurPos.getX(), selectionStartPos.getY(), selectionCurPos.getY(), SELECTION_COLOR);
        super.drawHorizontalLine(matrices, selectionStartPos.getX(), selectionCurPos.getX(), selectionCurPos.getY(), SELECTION_COLOR);
        super.drawVerticalLine(matrices, selectionStartPos.getX(), selectionStartPos.getY(), selectionCurPos.getY(), SELECTION_COLOR);
    }

    @Override
    boolean isHovered() {
        return false;
    }
}
