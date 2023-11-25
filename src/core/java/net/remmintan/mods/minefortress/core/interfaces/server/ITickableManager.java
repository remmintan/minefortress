package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ITickableManager {

    void tick(ServerPlayerEntity player);

}
