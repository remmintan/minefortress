package org.minefortress.controls;

import net.minecraft.client.MinecraftClient;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import org.minefortress.utils.ModUtils;

public class MouseEvents {
    private static boolean mousePressedInpreviousTick = false;
    private static long lastCheckTime = 0;

    public static void checkMouseStateAndFireEvents() {
        final var mouse = MinecraftClient.getInstance().mouse;
        final var isMousePressed = mouse.wasLeftButtonClicked();
        // mouse x and y coordinates
        final var mouseX = mouse.getX();
        final var mouseY = mouse.getY();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime <= 300) {
            if (isMousePressed) {
                if (!mousePressedInpreviousTick) {
                    if (ModUtils.getFortressClient().get_FortressHud().isHovered() || MinecraftClient.getInstance().currentScreen != null)
                        return;
                    firePressEvent(mouseX, mouseY);
                }
            } else {
                if (mousePressedInpreviousTick) {
                    fireReleaseEvent(mouseX, mouseY);
                }
            }
        }
        mousePressedInpreviousTick = isMousePressed;
        lastCheckTime = currentTime;
    }

    private static void firePressEvent(double mouseX, double mouseY) {
        final var fortressManager = ClientModUtils.getFortressManager();
        final var state = fortressManager.getState();
        final var correctState = state == FortressState.COMBAT || state == FortressState.BUILD_SELECTION;
        final var noBuildingHovered = !ClientModUtils.getBuildingsManager().isBuildingHovered();
        final var noScreenOpened = MinecraftClient.getInstance().currentScreen == null;
        final var hudNotHovered = !ModUtils.getFortressClient().get_FortressHud().isHovered();

        if (correctState && noBuildingHovered && noScreenOpened && hudNotHovered) {
            final var provider = ClientModUtils.getManagersProvider();
            final var pawnsSelection = provider.get_PawnsSelectionManager();
            pawnsSelection.startSelection(mouseX, mouseY);
        }
    }

    private static void fireReleaseEvent(double mouseX, double mouseY) {
        final var provider = ClientModUtils.getManagersProvider();
        final var pawnsSelection = provider.get_PawnsSelectionManager();
        pawnsSelection.endSelection(mouseX, mouseY);
    }
}