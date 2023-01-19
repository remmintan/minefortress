package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.fortress.FortressState;
import org.minefortress.utils.ModUtils;

import java.util.Optional;

public class HoveredEntityHudLayer extends AbstractHudLayer{
    protected HoveredEntityHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.RIGHT, PositionY.TOP);
    }

    @Override
    protected void renderHud(MatrixStack matrices, TextRenderer font, int screenWidth, int screenHeight) {
        getHoveredEntityName()
                .map(name -> name.replace("house", "").replace("House", ""))
                .ifPresent(name -> {
                    final int colonistWinX = 0;
                    final int colonistWinY = screenHeight - 50;
                    int width = 120;
                    final int height = 20;
                    DrawableHelper.fillGradient(
                            matrices,
                            colonistWinX,
                            colonistWinY,
                            colonistWinX + width,
                            colonistWinY + height,
                            0xc0101010,
                            0xd0101010,
                            -1000
                    );

                    Screen.drawCenteredText(
                            matrices,
                            font,
                            name,
                            colonistWinX + width / 2,
                            colonistWinY + 5,
                            0xFFFFFF
                    );
                });
    }

    private static Optional<String> getHoveredEntityName() {
        final var fortressManager = ModUtils.getFortressClientManager();
        if(fortressManager.getState() == FortressState.AREAS_SELECTION)
            return ModUtils.getAreasClientManager().getHoveredAreaName();
        return fortressManager.getHoveredBuildingName();
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD;
    }
}
