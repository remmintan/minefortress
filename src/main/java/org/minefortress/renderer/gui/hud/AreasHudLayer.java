package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import org.minefortress.areas.ProfessionsSelectionType;
import org.minefortress.renderer.gui.widget.ModeButtonWidget;
import org.minefortress.utils.ModUtils;

public class AreasHudLayer extends AbstractHudLayer {

    public AreasHudLayer(MinecraftClient client) {
        super(client);
        final var areasClientManager = ModUtils.getAreasClientManager();
        final var selectionType = areasClientManager.getSelectionType();
        for (final ProfessionsSelectionType type : ProfessionsSelectionType.values()) {
            this.addElement(
                new ModeButtonWidget(
                    35 + 20 * type.ordinal(), 0,
                    type.getIcon(),
                    btn -> areasClientManager.setSelectionType(type),
                    type.getTitle(),
                    () -> type == selectionType
                )
            );
        }

        this.setBasepoint(-91, -43, PositionX.CENTER, PositionY.BOTTOM);
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.AREAS_SELECTION;
    }
}
