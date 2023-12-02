package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface ITickableManager {

    void tick(@Nullable ServerPlayerEntity player);

}
