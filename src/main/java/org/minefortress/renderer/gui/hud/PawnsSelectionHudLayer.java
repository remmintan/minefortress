package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;

class PawnsSelectionHudLayer extends AbstractHudLayer {

    private static final int SELECTION_COLOR = 0xFF00FF00;
//    private static final int CROSS_COLOR = 0xFF808080;

    PawnsSelectionHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.CENTER, PositionY.BOTTOM);
    }

    @Override
    protected void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {
        renderCurrentSelection(drawContext);
    }

    private void renderCurrentSelection(DrawContext drawContext) {
        final var pawnsSelectionManager = ClientModUtils.getManagersProvider().get_PawnsSelectionManager();
        if(pawnsSelectionManager.isSelecting()) {
            final var selectionStartPos = pawnsSelectionManager.getMouseStartPos();
            final var selectionCurPos = pawnsSelectionManager.getMouseEndPos();

            final var widthScaleFactor = (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
            final var heightScaleFactor = (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();

            final var selectionStartX = (int) (selectionStartPos.x() * widthScaleFactor);
            final var selectionStartY = (int) (selectionStartPos.y() * heightScaleFactor);
            final var selectionCurX = (int) (selectionCurPos.x() * widthScaleFactor);
            final var selectionCurY = (int) (selectionCurPos.y() * heightScaleFactor);

            drawContext.drawHorizontalLine(selectionStartX, selectionCurX, selectionStartY, SELECTION_COLOR);
            drawContext.drawVerticalLine(selectionCurX, selectionStartY, selectionCurY, SELECTION_COLOR);
            drawContext.drawHorizontalLine(selectionStartX, selectionCurX, selectionCurY, SELECTION_COLOR);
            drawContext.drawVerticalLine(selectionStartX, selectionStartY, selectionCurY, SELECTION_COLOR);

//            pawnsSelectionManager.getScreenPositions().forEach(screenPos -> {
//                final var x = (int) (screenPos.x * widthScaleFactor);
//                final var y = (int) (screenPos.y * heightScaleFactor);
//                drawContext.drawHorizontalLine(x - 2, x + 2, y, CROSS_COLOR);
//                drawContext.drawVerticalLine(x, y - 2, y + 2, CROSS_COLOR);
//            });

        }
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.COMBAT || hudState == HudState.BUILD;
    }
}
