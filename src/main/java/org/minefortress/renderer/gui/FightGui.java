package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.fight.ClientFightSelectionManager;
import org.minefortress.interfaces.FortressMinecraftClient;

class FightGui extends FortressGuiScreen{

    private static final int SELECTION_COLOR = 0xFF00FF00;

    FightGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
    }

    @Override
    void render(MatrixStack matrices, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        final var fightSelectionManager = getFightSelectionManager();
        if(!fightSelectionManager.isSelecting()) return;
        final var selectionStartPos = fightSelectionManager.getSelectionStartPos();
        final var selectionCurPos = fightSelectionManager.getSelectionCurPos();

        final var widthScaleFactor = (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
        final var heightScaleFactor = (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();

        final var selectionStartX = (int) (selectionStartPos.x() * widthScaleFactor);
        final var selectionStartY = (int) (selectionStartPos.y() * heightScaleFactor);
        final var selectionCurX = (int) (selectionCurPos.x() * widthScaleFactor);
        final var selectionCurY = (int) (selectionCurPos.y() * heightScaleFactor);

        super.drawHorizontalLine(matrices, selectionStartX, selectionCurX, selectionStartY, SELECTION_COLOR);
        super.drawVerticalLine(matrices, selectionCurX, selectionStartY, selectionCurY, SELECTION_COLOR);
        super.drawHorizontalLine(matrices, selectionStartX, selectionCurX, selectionCurY, SELECTION_COLOR);
        super.drawVerticalLine(matrices, selectionStartX, selectionStartY, selectionCurY, SELECTION_COLOR);
    }

    @Override
    boolean isHovered() {
        return false;
    }

    private ClientFightSelectionManager getFightSelectionManager() {
        return ((FortressMinecraftClient)this.client).getFortressClientManager().getFightManager().getSelectionManager();
    }
}
