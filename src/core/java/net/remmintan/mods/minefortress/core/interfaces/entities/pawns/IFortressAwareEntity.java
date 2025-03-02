package net.remmintan.mods.minefortress.core.interfaces.entities.pawns;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface IFortressAwareEntity {

    @NotNull
    BlockPos getFortressPos();

    @NotNull
    MinecraftServer getServer();

}
