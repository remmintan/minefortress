package org.minefortress.renderer.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.text.Text;
import org.minefortress.entity.Colonist;
import org.minefortress.professions.Profession;
import org.minefortress.utils.ModUtils;

import java.util.Optional;

public class SelectedColonistHudLayer extends AbstractHudLayer{

    protected SelectedColonistHudLayer(MinecraftClient client) {
        super(client);
    }

    @Override
    protected void renderHud(MatrixStack matrices, TextRenderer font, int screenWidth, int screenHeight) {
        final var fortressManager = ModUtils.getFortressClientManager();
        if(fortressManager.isSelectingColonist()){
            final Colonist selectedColonist = fortressManager.getSelectedColonist();

            final int colonistWinX = 0;
            final int colonistWinY = screenHeight - 85;
            int width = 120;
            final int height = 85;
            DrawableHelper.fillGradient(matrices, colonistWinX, colonistWinY, colonistWinX + width, colonistWinY + height, 0xc0101010, 0xd0101010, 100);

            final String name = Optional.ofNullable(selectedColonist.getCustomName()).map(Text::asString).orElse("");
            final String healthString = String.format("%.0f/%.0f", selectedColonist.getHealth(), selectedColonist.getMaxHealth());
            final String hungerString = String.format("%d/%d", selectedColonist.getCurrentFoodLevel(), HungerConstants.FULL_FOOD_LEVEL);
            final String professionId = selectedColonist.getProfessionId();
            final String professionName = Optional.ofNullable(fortressManager.getProfessionManager().getProfession(professionId)).map(Profession::getTitle).orElse("");
            final String task = selectedColonist.getCurrentTaskDesc();

            width = Math.max(width, font.getWidth(healthString));
            width = Math.max(width, font.getWidth(hungerString));
            width = Math.max(width, font.getWidth(professionName));
            width = Math.max(width, font.getWidth(task));

            Screen.drawCenteredText(matrices, font, name, colonistWinX + width / 2, colonistWinY + 5, 0xFFFFFF);

            int heartIconX = colonistWinX + 5;
            int heartIconY = colonistWinY + textRenderer.fontHeight + 10;
            renderIcon(matrices, heartIconX, heartIconY, 8, 8, 52, 0);
            textRenderer.draw(matrices, healthString, heartIconX + 10, heartIconY + 2, 0xFFFFFF);

            int hungerIconX = colonistWinX + width/2 + 5;
            renderIcon(matrices, hungerIconX, heartIconY, 8, 8, 52, 28);
            textRenderer.draw(matrices, hungerString, hungerIconX + 10, heartIconY + 2, 0xFFFFFF);

            textRenderer.draw(matrices, "Profession:", colonistWinX + 5, heartIconY + textRenderer.fontHeight + 5, 0xFFFFFF);
            textRenderer.draw(matrices, professionName, colonistWinX + 5, heartIconY + 2 * textRenderer.fontHeight + 5 , 0xFFFFFF);

            textRenderer.draw(matrices, "Task:", colonistWinX + 5, heartIconY + 3 * textRenderer.fontHeight + 10, 0xFFFFFF);
            textRenderer.draw(matrices, task, colonistWinX + 5, heartIconY + 4 * textRenderer.fontHeight + 10, 0xFFFFFF);
        }
    }

    private void renderIcon(MatrixStack matrices, int iconX, int iconY, int iconWidth, int iconHeight, int heartIconU, int heartIconV) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        DrawableHelper.drawTexture(matrices, iconX, iconY, 110, heartIconU, heartIconV, iconWidth, iconHeight, 256, 256);
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD;
    }
}
