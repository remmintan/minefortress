package net.remmintan.mods.minefortress.core.interfaces.entities.pawns;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFortressAwareEntity {

    @Nullable
    BlockPos getFortressPos();

    @NotNull
    MinecraftServer getServer();

}
