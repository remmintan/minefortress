package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.remmintan.mods.minefortress.core.interfaces.automation.ProfessionsSelectionType;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;
import net.remmintan.mods.minefortress.gui.widget.hud.ModeButtonWidget;

public class AreasHudLayer extends AbstractHudLayer {

    public AreasHudLayer(MinecraftClient client) {
        super(client);
        for (final ProfessionsSelectionType type : ProfessionsSelectionType.values()) {
            this.addElement(
                new ModeButtonWidget(
                     20 * type.ordinal(), 0,
                    type.getIcon(),
                        btn -> ClientModUtils.getAreasClientManager().setSelectionType(type),
                    type.getTitle(),
                        () -> type == ClientModUtils.getAreasClientManager().getSelectionType()
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
