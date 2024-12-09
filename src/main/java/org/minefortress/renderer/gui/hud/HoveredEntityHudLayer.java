package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;

import java.util.Optional;

public class HoveredEntityHudLayer extends AbstractHudLayer{
    protected HoveredEntityHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 0, PositionX.RIGHT, PositionY.TOP);
    }

    @Override
    protected void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {
        getHoveredEntityName()
                .map(name -> name.replace("house", "").replace("House", ""))
                .ifPresent(name -> {
                    final int colonistWinX = 0;
                    final int colonistWinY = screenHeight - 50;
                    int width = 120;
                    final int height = 20;
                    drawContext.fillGradient(
                            colonistWinX,
                            colonistWinY,
                            colonistWinX + width,
                            colonistWinY + height,
                            -1000,
                            0xc0101010,
                            0xd0101010
                    );

                    drawContext.drawCenteredTextWithShadow(
                            textRenderer,
                            name,
                            colonistWinX + width / 2,
                            colonistWinY + 5,
                            0xFFFFFF
                    );
                });
    }

    private static Optional<String> getHoveredEntityName() {
        final var fortressManager = CoreModUtils.getFortressClientManager();
        if(fortressManager.getState() == FortressState.AREAS_SELECTION)
            return CoreModUtils.getAreasClientManager().getHoveredAreaName();

        return CoreModUtils.getBuildingsManager().getHoveredBuildingName();
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD || hudState == HudState.AREAS_SELECTION;
    }
}
