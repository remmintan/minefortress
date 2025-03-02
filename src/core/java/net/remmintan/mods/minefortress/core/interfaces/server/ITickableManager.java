package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ITickableManager {

    void tick(@NotNull MinecraftServer server, @NotNull ServerWorld world, @Nullable ServerPlayerEntity player);

}
