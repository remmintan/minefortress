package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.UUID;

public interface IFortressModServerManager {
    IServerManagersProvider getManagersProvider(ServerPlayerEntity player);
    IServerManagersProvider getManagersProvider(UUID id);
    IServerFortressManager getFortressManager(ServerPlayerEntity player);
    IServerFortressManager getFortressManager(UUID id);
    boolean isCampfireEnabled();
    boolean isBorderEnabled();
    void save();
    void load();
    void load(boolean campfire, boolean border);
    void tick(PlayerManager manager);
    Optional<IServerManagersProvider> findReachableFortress(BlockPos pos, double reachRange);
}
