package org.minefortress.controls;

import net.minecraft.client.MinecraftClient;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.minefortress.utils.ModUtils;

public class MouseEvents {

    private static boolean mousePressedInpreviousTick = false;

    public static void checkMouseStateAndFireEvents() {
        final var mouse = MinecraftClient.getInstance().mouse;
        final var isMousePressed = mouse.wasLeftButtonClicked();
        // mouse x and y coordinates
        final var mouseX = mouse.getX();
        final var mouseY = mouse.getY();
        if(isMousePressed) {
            if(!mousePressedInpreviousTick) {
                if(ModUtils.getFortressClient().get_FortressHud().isHovered() || MinecraftClient.getInstance().currentScreen != null)
                    return;
                firePressEvent(mouseX, mouseY);
            }
        } else {
            if(mousePressedInpreviousTick) {
                fireReleaseEvent(mouseX, mouseY);
            }
        }
        mousePressedInpreviousTick = isMousePressed;
    }

    private static void firePressEvent(double mouseX, double mouseY) {
        final var fortressManager = ModUtils.getFortressClientManager();
        final var state = fortressManager.getState();
        final var correctState = state == FortressState.COMBAT || state == FortressState.BUILD_SELECTION;
        final var noBuildingHovered = ModUtils.getFortressClientManager().getHoveredBuilding().isEmpty();
        final var noScreenOpened = MinecraftClient.getInstance().currentScreen == null;

        if(correctState && noBuildingHovered && noScreenOpened) {
            final var provider = CoreModUtils.getMineFortressManagersProvider();
            final var pawnsSelection = provider.get_PawnsSelectionManager();
            pawnsSelection.startSelection(mouseX, mouseY);
        }
    }

    private static void fireReleaseEvent(double mouseX, double mouseY) {
        final var provider = CoreModUtils.getMineFortressManagersProvider();
        final var pawnsSelection = provider.get_PawnsSelectionManager();
        pawnsSelection.endSelection(mouseX, mouseY);
    }

}
