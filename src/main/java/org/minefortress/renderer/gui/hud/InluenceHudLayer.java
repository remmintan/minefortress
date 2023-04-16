package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import org.minefortress.renderer.gui.widget.ItemToggleWidget;
import org.minefortress.utils.ModUtils;

import java.util.Optional;

public class InluenceHudLayer extends AbstractHudLayer {

    private final ItemToggleWidget captureButton;

    protected InluenceHudLayer(MinecraftClient client) {
        super(client);
        setBasepoint(5 , 0, PositionX.LEFT, PositionY.CENTER);

        captureButton = new ItemToggleWidget(
                0,
                0,
                Items.RED_BANNER,
                btn -> {
                    final var influenceManager = ModUtils.getInfluenceManager();
                    if (influenceManager.isSelecting()) {
                        influenceManager.cancelSelectingInfluencePosition();
                    } else {
                        influenceManager.startSelectingInfluencePosition();
                    }
                },
                (button) -> Optional.of("Influence"),
                () -> ModUtils.getInfluenceManager().isSelecting(),
                () -> true
        );
        this.addElement(captureButton);
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.COMBAT;
    }

    @Override
    public void tick() {
        this.captureButton.visible = !ModUtils.getFortressClientManager().isCreative();
    }
}
