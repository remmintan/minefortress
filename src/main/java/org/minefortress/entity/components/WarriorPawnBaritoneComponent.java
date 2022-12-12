package org.minefortress.entity.components;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import org.minefortress.entity.WarriorPawn;

public class WarriorPawnBaritoneComponent implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        AbstractBaritoneComponent.registerGlobalSettings();
        registry.registerFor(WarriorPawn.class, IBaritone.KEY, BaritoneAPI.getProvider().componentFactory());
    }
}
