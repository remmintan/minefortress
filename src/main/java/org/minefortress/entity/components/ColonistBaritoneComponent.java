package org.minefortress.entity.components;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import org.minefortress.entity.Colonist;

public class ColonistBaritoneComponent implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        AbstractBaritoneComponent.registerGlobalSettings();
        registry.registerFor(Colonist.class, IBaritone.KEY, BaritoneAPI.getProvider().componentFactory());
    }
}
