package org.minefortress.renderer.gui.hud.hints;


import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;
import net.remmintan.mods.minefortress.gui.hud.interfaces.IHintsLayer;
import org.minefortress.renderer.gui.hud.AbstractHudLayer;
import org.minefortress.renderer.gui.hud.FortressHud;

import java.util.List;
import java.util.Optional;

public abstract class AbstractHintsLayer extends AbstractHudLayer implements IHintsLayer {

    public AbstractHintsLayer() {
        super(MinecraftClient.getInstance());
        this.setBasepoint(0, 0, PositionX.LEFT, PositionY.TOP);
    }

    protected abstract List<String> getHints();

    protected Optional<String> getInfoText() {
        return Optional.empty();
    }

    @Override
    protected final void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {
        final var hints = Lists.reverse(getHints());
        for (int i = 0; i < hints.size(); i++) {
            final var hint = hints.get(i);
            final var y = screenHeight - 15 - (i * 10);
            drawContext.drawTextWithShadow(textRenderer, hint, 5, y, FortressHud.MOD_GUI_COLOR);
        }

        getInfoText().ifPresent(info -> drawContext.drawTextWithShadow(textRenderer, info, 5, 5, FortressHud.MOD_GUI_COLOR));
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return !ClientModUtils.getManagersProvider().getSelectedColonistProvider().isSelectingColonist();
    }
}
