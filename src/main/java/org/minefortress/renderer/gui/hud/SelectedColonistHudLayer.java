package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IHungerAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IProfessional;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfession;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.minefortress.entity.Colonist;
import org.minefortress.utils.ModUtils;

import java.util.Optional;

public class SelectedColonistHudLayer extends AbstractHudLayer{

    private static final Identifier HEART_TEXTURE = new Identifier("hud/heart/full");
    private static final Identifier FOOD_TEXTURE = new Identifier("hud/food_full");

    protected SelectedColonistHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.RIGHT, PositionY.TOP);
    }

    @Override
    protected void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {
        final var selectedPawnsProvider = CoreModUtils.getMineFortressManagersProvider().getSelectedColonistProvider();
        if(selectedPawnsProvider.isSelectingColonist()) {
            final var pawn = selectedPawnsProvider.getSelectedPawn();

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
            drawContext.drawGuiTexture(HEART_TEXTURE, heartIconX, heartIconY, 9, 9);
            drawContext.drawTextWithShadow(textRenderer, healthString, heartIconX + 10, heartIconY + 2, 0xFFFFFF);

            if(pawn instanceof IHungerAwareEntity hungerAwareEntity) {
                final String hungerString = String.format("%d/%d", hungerAwareEntity.getCurrentFoodLevel(), HungerConstants.FULL_FOOD_LEVEL);
                int hungerIconX = colonistWinX + width/2 + 5;
                drawContext.drawGuiTexture(FOOD_TEXTURE, hungerIconX, heartIconY, 9, 9);
                drawContext.drawTextWithShadow(textRenderer, hungerString, hungerIconX + 10, heartIconY + 2, 0xFFFFFF);
            }

            if(pawn instanceof IProfessional professional) {
                final var professionId = professional.getProfessionId();
                final var professionManager = ModUtils.getProfessionManager();
                final var professionName = Optional.ofNullable(professionManager.getProfession(professionId)).map(IProfession::getTitle).orElse("");
                drawContext.drawTextWithShadow(textRenderer, "Profession:", colonistWinX + 5, heartIconY + textRenderer.fontHeight + 5, 0xFFFFFF);
                drawContext.drawTextWithShadow(textRenderer, professionName, colonistWinX + 5, heartIconY + 2 * textRenderer.fontHeight + 5 , 0xFFFFFF);
            }

            if(pawn instanceof Colonist colonist) {
                drawContext.drawTextWithShadow(textRenderer, "Task:", colonistWinX + 5, heartIconY + 3 * textRenderer.fontHeight + 10, 0xFFFFFF);
                final var task = colonist.getCurrentTaskDesc();
                drawContext.drawTextWithShadow(textRenderer, task, colonistWinX + 5, heartIconY + 4 * textRenderer.fontHeight + 10, 0xFFFFFF);
            }
        }
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD;
    }
}
