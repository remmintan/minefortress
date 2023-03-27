package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import org.minefortress.renderer.gui.widget.ItemToggleWidget;

import java.util.Optional;

public class InluenceHudLayer extends AbstractHudLayer {

    protected InluenceHudLayer(MinecraftClient client) {
        super(client);
        setBasepoint(5 , 0, PositionX.LEFT, PositionY.CENTER);

        this.addElement(
                new ItemToggleWidget(
                        0,
                        0,
                        Items.RED_BANNER,
                        btn -> {

                        },
                        (button) -> Optional.of("Influence"),
                        () -> false,
                        () -> true
                )
        );
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.COMBAT;
    }
}
