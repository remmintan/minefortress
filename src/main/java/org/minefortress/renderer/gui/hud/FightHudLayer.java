package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRotation;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightSelectionManager;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.utils.ModUtils;

import static org.minefortress.renderer.gui.blueprints.BlueprintsScreen.convertItemIconInTheGUI;

class FightHudLayer extends AbstractHudLayer {

    private static final int SELECTION_COLOR = 0xFF00FF00;

    FightHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.LEFT, PositionY.TOP);
    }

    @Override
    protected void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {
        final var influenceManager = ModUtils.getInfluenceManager();
        if(influenceManager.isSelecting() && ModUtils.getFortressClientManager().isSurvival()) {
            final var stacks = influenceManager.getBlockDataProvider()
                    .getBlockData("influence_flag", BlockRotation.NONE)
                    .getStacks();

            final var resourceManager = ModUtils.getFortressClientManager().getResourceManager();

            for (int i1 = 0; i1 < stacks.size(); i1++) {
                final ItemInfo stack = stacks.get(i1);
                final var hasItem = resourceManager.hasItem(stack, stacks);
                final var itemX = screenWidth/2 - 55 + i1%10 * 30;
                final var itemY = i1/10 * 20 + screenHeight - 40;
                final var convertedItem = convertItemIconInTheGUI(stack);

                drawContext.drawItem(new ItemStack(convertedItem), itemX, itemY);
                drawContext.drawText(this.textRenderer, String.valueOf(stack.amount()), itemX + 17, itemY + 7, hasItem?0xFFFFFF:0xFF0000, false);
            }
        }

        final var fightSelectionManager = getFightSelectionManager();
        if(fightSelectionManager.isSelecting()) {
            final var selectionStartPos = fightSelectionManager.getSelectionStartPos();
            final var selectionCurPos = fightSelectionManager.getSelectionCurPos();

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
        }
    }

    private IClientFightSelectionManager getFightSelectionManager() {
        return ModUtils.getFortressClientManager().getFightManager().getSelectionManager();
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.COMBAT;
    }
}
