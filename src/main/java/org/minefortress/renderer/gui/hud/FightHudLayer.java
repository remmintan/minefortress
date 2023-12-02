package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightSelectionManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.minefortress.renderer.gui.widget.ItemHudElement;
import org.minefortress.utils.ModUtils;

import static org.minefortress.renderer.gui.blueprints.BlueprintsScreen.convertItemIconInTheGUI;

class FightHudLayer extends AbstractHudLayer {

    private static final int SELECTION_COLOR = 0xFF00FF00;

    FightHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.CENTER, PositionY.BOTTOM);
        this.addElement(
                new ItemHudElement(
                        0,
                        0,
                        Items.STONE_SWORD,
                        () -> "x" + getFightManager().getWarriorCount()
                )
        );
    }

    @Override
    protected void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {
        drawTotalWarriorsAmount(drawContext, screenWidth, screenHeight);
        renderInfluenceFlagCosts(drawContext, screenWidth, screenHeight);
        renderCurrentSelection(drawContext);
    }

    private void drawTotalWarriorsAmount(DrawContext drawContext, int screenWidth, int screenHeight) {
        final var totalWarriorsAmountX = screenWidth/2 - 55;
        final var totalWarriorsAmountY = screenHeight - 40;
        final var warriorCountText = "x" + getFightManager().getWarriorCount();

        drawContext.drawItem(Items.STONE_SWORD.getDefaultStack(), totalWarriorsAmountX, totalWarriorsAmountY);
        drawContext.drawText(this.textRenderer, Text.literal(warriorCountText), totalWarriorsAmountX + 17, totalWarriorsAmountY + 7, 0xFFFFFF, false);
    }

    private void renderInfluenceFlagCosts(DrawContext drawContext, int screenWidth, int screenHeight) {
        final var influenceManager = CoreModUtils.getMineFortressManagersProvider().get_InfluenceManager();
        if(influenceManager.isSelecting() && ModUtils.getFortressClientManager().isSurvival()) {
            final var stacks = influenceManager.getBlockDataProvider()
                    .getBlockData("influence_flag", BlockRotation.NONE)
                    .getStacks();

            final var resourceManager = ModUtils.getFortressClientManager().getResourceManager();

            for (int i1 = 0; i1 < stacks.size(); i1++) {
                final var stack = stacks.get(i1);
                final var hasItem = resourceManager.hasItem(stack, stacks);
                final var itemX = screenWidth /2 - 55 + i1%10 * 30;
                final var itemY = i1/10 * 20 + screenHeight - 40;
                final var convertedItem = convertItemIconInTheGUI(stack);

                drawContext.drawItem(new ItemStack(convertedItem), itemX, itemY);
                drawContext.drawText(this.textRenderer, String.valueOf(stack.amount()), itemX + 17, itemY + 7, hasItem?0xFFFFFF:0xFF0000, false);
            }
        }
    }

    private void renderCurrentSelection(DrawContext drawContext) {
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
        return getFightManager().getSelectionManager();
    }

    private static IClientFightManager getFightManager() {
        return ModUtils.getFortressClientManager().getFightManager();
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.COMBAT;
    }
}
