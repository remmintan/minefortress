package org.minefortress.renderer.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.text.Text;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.interfaces.IProfessional;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.professions.Profession;
import org.minefortress.utils.ModUtils;

import java.util.Optional;

public class SelectedColonistHudLayer extends AbstractHudLayer{

    protected SelectedColonistHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.RIGHT, PositionY.TOP);
    }

    @Override
    protected void renderHud(DrawContext drawContext, TextRenderer font, int screenWidth, int screenHeight) {
        final var fortressManager = ModUtils.getFortressClientManager();
        if(fortressManager.isSelectingColonist()){
            final var pawn = fortressManager.getSelectedPawn();

            final int colonistWinX = 0;
            final int colonistWinY = screenHeight - 85;
            int width = 120;
            final int height = 85;
            drawContext.fillGradient(colonistWinX, colonistWinY, colonistWinX + width, colonistWinY + height, 0xc0101010, 0xd0101010, -1000);

            final String name = Optional.ofNullable(pawn.getCustomName()).map(Text::getContent).orElse("");
            drawContext.drawCenteredTextWithShadow(font, name, colonistWinX + width / 2, colonistWinY + 5, 0xFFFFFF);

            final String healthString = String.format("%.0f/%.0f", pawn.getHealth(), pawn.getMaxHealth());
            int heartIconX = colonistWinX + 5;
            int heartIconY = colonistWinY + textRenderer.fontHeight + 10;
            renderIcon(matrices, heartIconX, heartIconY, 0);
            textRenderer.draw(matrices, healthString, heartIconX + 10, heartIconY + 2, 0xFFFFFF);

            final String hungerString = String.format("%d/%d", pawn.getCurrentFoodLevel(), HungerConstants.FULL_FOOD_LEVEL);
            int hungerIconX = colonistWinX + width/2 + 5;
            renderIcon(matrices, hungerIconX, heartIconY, 28);
            textRenderer.draw(matrices, hungerString, hungerIconX + 10, heartIconY + 2, 0xFFFFFF);

            if(pawn instanceof IProfessional professional) {
                final String professionId = professional.getProfessionId();
                final String professionName = Optional.ofNullable(fortressManager.getProfessionManager().getProfession(professionId)).map(Profession::getTitle).orElse("");
                textRenderer.draw(matrices, "Profession:", colonistWinX + 5, heartIconY + textRenderer.fontHeight + 5, 0xFFFFFF);
                textRenderer.draw(matrices, professionName, colonistWinX + 5, heartIconY + 2 * textRenderer.fontHeight + 5 , 0xFFFFFF);
            }


            if(pawn instanceof Colonist colonist) {
                textRenderer.draw(matrices, "Task:", colonistWinX + 5, heartIconY + 3 * textRenderer.fontHeight + 10, 0xFFFFFF);
                final String task = colonist.getCurrentTaskDesc();
                textRenderer.draw(matrices, task, colonistWinX + 5, heartIconY + 4 * textRenderer.fontHeight + 10, 0xFFFFFF);
            }
        }
    }

    private void renderIcon(DrawContext drawContext, int iconX, int iconY, int heartIconV) {
        drawContext.drawTexture(GUI_ICONS_TEXTURE, iconX, iconY, 110, 52, heartIconV, 8, 8, 256, 256);
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD;
    }
}
