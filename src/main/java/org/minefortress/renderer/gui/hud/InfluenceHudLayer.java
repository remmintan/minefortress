package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.infuence.IClientInfluenceManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.minefortress.fight.influence.ClientInfluenceManager;
import org.minefortress.renderer.gui.widget.ItemButtonWidget;
import org.minefortress.renderer.gui.widget.ItemToggleWidget;

import java.util.Optional;

public class InfluenceHudLayer extends AbstractHudLayer {

    private final ItemToggleWidget captureButton;

    protected InfluenceHudLayer(MinecraftClient client) {
        super(client);
        setBasepoint(-25 , 0, PositionX.RIGHT, PositionY.CENTER);

        captureButton = new ItemToggleWidget(
                0,
                -13,
                Items.RED_BANNER,
                btn -> {
                    final var influenceManager = getInfluenceManager();
                    if (influenceManager.isSelecting()) {
                        influenceManager.cancelSelectingInfluencePosition();
                    } else {
                        influenceManager.startSelectingInfluencePosition();
                    }
                },
                (button) -> Optional.of("Influence"),
                () -> getInfluenceManager().isSelecting(),
                () -> true
        );
        this.addElement(captureButton);

        this.addElement(
            new ItemButtonWidget(
                    0,
                    13,
                    Items.CAMPFIRE,
                    (btn) -> getFightManager().attractWarriorsToCampfire(),
                    "Call warriors to fortress center"
            )
        );
    }

    private static IClientInfluenceManager getInfluenceManager() {
        return getMineFortressManagersProvider().get_InfluenceManager();
    }

    private static IClientFightManager getFightManager() {
        return getMineFortressManagersProvider().get_ClientFortressManager().getFightManager();
    }

    private static IClientManagersProvider getMineFortressManagersProvider() {
        return CoreModUtils.getMineFortressManagersProvider();
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.COMBAT;
    }

    @Override
    public void tick() {
        this.captureButton.visible = ClientInfluenceManager.influenceEnabled();
    }
}
