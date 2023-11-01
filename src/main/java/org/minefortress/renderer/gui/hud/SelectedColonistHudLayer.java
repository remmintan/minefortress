package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IHungerAwareEntity;
import org.minefortress.entity.Colonist;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IProfessional;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfession;
import org.minefortress.utils.ModUtils;
import java.util.Optional;

public class SelectedColonistHudLayer extends AbstractHudLayer{

    protected SelectedColonistHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.RIGHT, PositionY.TOP);
    }

    @Override
    protected void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {
        final var fortressManager = ModUtils.getFortressClientManager();
        if(fortressManager.isSelectingColonist()){
            final var pawn = fortressManager.getSelectedPawn();

            final int colonistWinX = 0;
            final int colonistWinY = screenHeight - 85;
            int width = 120;
            final int height = 85;
            drawContext.fillGradient(colonistWinX, colonistWinY, colonistWinX + width, colonistWinY + height, -1000,0xc0101010, 0xd0101010);

            final String name = Optional.ofNullable(pawn.getCustomName()).map(Text::getString).orElse("");
            drawContext.drawCenteredTextWithShadow(textRenderer, name, colonistWinX + width / 2, colonistWinY + 5, 0xFFFFFF);

            final String healthString = String.format("%.0f/%.0f", pawn.getHealth(), pawn.getMaxHealth());
            int heartIconX = colonistWinX + 5;
            int heartIconY = colonistWinY + textRenderer.fontHeight + 10;
            renderIcon(drawContext, heartIconX, heartIconY, 0);
            drawContext.drawTextWithShadow(textRenderer, healthString, heartIconX + 10, heartIconY + 2, 0xFFFFFF);


            if(pawn instanceof IHungerAwareEntity hungerAwareEntity) {
                final String hungerString = String.format("%d/%d", hungerAwareEntity.getCurrentFoodLevel(), HungerConstants.FULL_FOOD_LEVEL);
                int hungerIconX = colonistWinX + width/2 + 5;
                renderIcon(drawContext, hungerIconX, heartIconY, 28);
                drawContext.drawTextWithShadow(textRenderer, hungerString, hungerIconX + 10, heartIconY + 2, 0xFFFFFF);
            }

            if(pawn instanceof IProfessional professional) {
                final String professionId = professional.getProfessionId();
                final String professionName = Optional.ofNullable(fortressManager.getProfessionManager().getProfession(professionId)).map(IProfession::getTitle).orElse("");
                drawContext.drawTextWithShadow(textRenderer, "Profession:", colonistWinX + 5, heartIconY + textRenderer.fontHeight + 5, 0xFFFFFF);
                drawContext.drawTextWithShadow(textRenderer, professionName, colonistWinX + 5, heartIconY + 2 * textRenderer.fontHeight + 5 , 0xFFFFFF);
            }


            if(pawn instanceof Colonist colonist) {
                drawContext.drawTextWithShadow(textRenderer, "Task:", colonistWinX + 5, heartIconY + 3 * textRenderer.fontHeight + 10, 0xFFFFFF);
                final String task = colonist.getCurrentTaskDesc();
                drawContext.drawTextWithShadow(textRenderer, task, colonistWinX + 5, heartIconY + 4 * textRenderer.fontHeight + 10, 0xFFFFFF);
            }
        }
    }

    private void renderIcon(DrawContext drawContext, int iconX, int iconY, int heartIconV) {
        drawContext.drawTexture(InGameHud.HOTBAR_TEXTURE, iconX, iconY, 110, 52, heartIconV, 8, 8, 256, 256);
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD;
    }
}
