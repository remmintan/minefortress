package org.minefortress.earlyriser;

import com.chocohead.mm.api.ClassTinkerers;

public class GameModeEarlyRiser implements Runnable{
    @Override
    public void run() {
        ClassTinkerers
                .enumBuilder("net.minecraft.world.GameMode", int.class, String.class)
                .addEnum("FORTRESS", 4, "fortress")
                .build();
    }
}
