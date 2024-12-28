package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;
import net.remmintan.mods.minefortress.gui.widget.ItemHudElement;

class CombatHudLayer extends AbstractHudLayer{
    CombatHudLayer(MinecraftClient client) {
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
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.COMBAT;
    }

    private void drawTotalWarriorsAmount(DrawContext drawContext, int screenWidth, int screenHeight) {
        final var totalWarriorsAmountX = screenWidth/2 - 55;
        final var totalWarriorsAmountY = screenHeight - 40;
        final var warriorCountText = "x" + getFightManager().getWarriorCount();

        drawContext.drawItem(Items.STONE_SWORD.getDefaultStack(), totalWarriorsAmountX, totalWarriorsAmountY);
        drawContext.drawText(this.textRenderer, Text.literal(warriorCountText), totalWarriorsAmountX + 17, totalWarriorsAmountY + 7, 0xFFFFFF, false);
    }

    private static IClientFightManager getFightManager() {
        return CoreModUtils.getFortressManager().getFightManager();
    }

}
