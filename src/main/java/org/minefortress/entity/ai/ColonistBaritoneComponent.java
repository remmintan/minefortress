package org.minefortress.entity.ai;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import org.minefortress.entity.Colonist;

public class ColonistBaritoneComponent implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        final var globalSettings = BaritoneAPI.getGlobalSettings();
        globalSettings.allowParkour.set(true);
        globalSettings.allowSprint.set(true);
        globalSettings.allowSwimming.set(true);
        globalSettings.buildRepeatSneaky.set(true);

        registry.registerFor(Colonist.class, IBaritone.KEY, BaritoneAPI.getProvider().componentFactory());
    }
}
