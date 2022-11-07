package org.minefortress.entity.components;

import baritone.api.BaritoneAPI;

final class AbstractBaritoneComponent {

    private AbstractBaritoneComponent() {
        throw new IllegalStateException("Utility class");
    }

    static void registerGlobalSettings() {
        final var globalSettings = BaritoneAPI.getGlobalSettings();
        globalSettings.allowParkour.set(true);
        globalSettings.allowSprint.set(true);
        globalSettings.allowSwimming.set(true);
        globalSettings.buildRepeatSneaky.set(true);
        globalSettings.allowPlace.set(true);
        globalSettings.allowBreak.set(false);
        globalSettings.allowParkourPlace.set(true);
        globalSettings.allowInventory.set(true);
    }

}
