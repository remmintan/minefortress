package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;
import net.remmintan.mods.minefortress.gui.widget.hud.ModeButtonWidget;

public final class ModeHudLayer extends AbstractHudLayer{


    ModeHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 5, PositionX.CENTER, PositionY.TOP);
    }

    @Override
    protected void init() {
        final var fcm = ClientModUtils.getFortressManager();
        this.addElement(
                new ModeButtonWidget(
                        36,
                        0,
                        Items.STONE_PICKAXE,
                        (btn) -> fcm.setState(FortressState.AREAS_SELECTION),
                        "Zones Selection Mode",
                        () -> fcm.getState() == FortressState.AREAS_SELECTION
                ),
                new ModeButtonWidget(
                        -12,
                        0,
                        Items.STONE_SWORD,
                        (btn) -> fcm.setState(FortressState.COMBAT),
                        "Combat Mode",
                        () -> fcm.getState() == FortressState.COMBAT
                ),
                new ModeButtonWidget(
                        12,
                        0,
                        Items.STONE_SHOVEL,
                        (btn) -> fcm.setState(FortressState.BUILD_EDITING),
                        "Build Mode",
                        () -> fcm.getState() == FortressState.BUILD_EDITING || fcm.getState() == FortressState.BUILD_SELECTION
                )
        );
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState != HudState.BLANK && hudState != HudState.INITIALIZING && hudState != HudState.BLUEPRINT_EDITING;
    }

}