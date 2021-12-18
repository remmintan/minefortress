package org.minefortress.earlyriser;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class GameModeEarlyRiser implements Runnable{
    @Override
    public void run() {
        final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();

        final String gameMode = mappingResolver.mapClassName("intermediary", "net.minecraft.class_1934");

        ClassTinkerers
                .enumBuilder(gameMode, int.class, String.class)
                .addEnum("FORTRESS", 4, "fortress")
                .build();
    }
}
