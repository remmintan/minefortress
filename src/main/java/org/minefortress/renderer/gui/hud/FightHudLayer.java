package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.fight.ClientFightSelectionManager;
import org.minefortress.utils.ModUtils;

class FightHudLayer extends AbstractHudLayer {

    private static final int SELECTION_COLOR = 0xFF00FF00;

    FightHudLayer(MinecraftClient client) {
        super(client);
    }

    @Override
    protected void renderHud(MatrixStack matrices, TextRenderer font, int screenWidth, int screenHeight) {
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

    private ClientFightSelectionManager getFightSelectionManager() {
        return ModUtils.getFortressClientManager().getFightManager().getSelectionManager();
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.COMBAT;
    }
}
