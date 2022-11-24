package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import org.minefortress.renderer.gui.widget.ModeButtonWidget;
import org.minefortress.utils.ModUtils;

public class ModeHudLayer extends AbstractHudLayer{


    protected ModeHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(0, 5, PositionX.CENTER, PositionY.TOP);
        this.addElement(
                new ModeButtonWidget(
                        -10,
                        0,
                        Items.STONE_SWORD,
                        (btn) -> this.enableCombatModeIfDisabled(),
                        "Combat Mode",
                        () -> ModUtils.getFortressClientManager().isInCombat()
                ),
                new ModeButtonWidget(
                        10,
                        0,
                        Items.STONE_SHOVEL,
                        (btn) -> this.disableCombatModIfEnabled(),
                        "Build Mode",
                        () -> !ModUtils.getFortressClientManager().isInCombat()
                )
        );
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState != HudState.BLANK && hudState != HudState.INITIALIZING;
    }

    public void enableCombatModeIfDisabled(){
        final var clientManager = ModUtils.getFortressClientManager();
        if(!clientManager.isInCombat()){
            clientManager.setInCombat(true);
        }
    }

    public void disableCombatModIfEnabled(){
        final var clientManager = ModUtils.getFortressClientManager();
        if(clientManager.isInCombat()){
            clientManager.setInCombat(false);
        }
    }

}
