package org.minefortress.fortress.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ModLogger;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressModServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ModPathUtils;
import org.minefortress.fortress.ServerFortressManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FortressModServerManager implements IFortressModServerManager {

    private static final String MANAGERS_FILE_NAME = "server-managers.nbt";
    private final MinecraftServer server;
    private final Map<UUID, ServerFortressManager> serverManagers = new HashMap<>();

    private Boolean notEmpty = null;

    public FortressModServerManager(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public IServerManagersProvider getManagersProvider(ServerPlayerEntity player) {
        return getByPlayer(player);
    }

    private ServerFortressManager getByPlayer(ServerPlayerEntity player) {
        final var playerId = player.getGameProfile().getId();
        return serverManagers.computeIfAbsent(playerId, (it) -> new ServerFortressManager(server));
    }

    @Override
    public IServerManagersProvider getManagersProvider(UUID uuid) {
        return getByPlayer(uuid);
    }

    @Override
    public IServerFortressManager getFortressManager(ServerPlayerEntity player) {
        return getByPlayer(player);
    }

    @Override
    public IServerFortressManager getFortressManager(UUID id) {
        return getByPlayer(id);
    }

    private ServerFortressManager getByPlayer(UUID uuid) {
        if(serverManagers.containsKey(uuid)) {
            return serverManagers.get(uuid);
        } else {
            // TODO: write some fallback logic to find the player
            throw new IllegalArgumentException("No server manager found for player with id " + uuid);
        }
    }

    public void tick(PlayerManager playerManager) {
        for (Map.Entry<UUID, ServerFortressManager> entry : serverManagers.entrySet()) {
            final var playerId = entry.getKey();
            final var manager = entry.getValue();
            final var player = playerManager.getPlayer(playerId);
            manager.tick(player);
        }
    }

    public void save() {
        final var nbt = new NbtCompound();
        for (Map.Entry<UUID, ServerFortressManager> entry : serverManagers.entrySet()) {
            final var fortressNbt = new NbtCompound();
            final var id = entry.getKey();
            final var manager = entry.getValue();
            manager.writeToNbt(fortressNbt);
            nbt.put(id.toString(), fortressNbt);
        }

        ModPathUtils.saveNbt(nbt, MANAGERS_FILE_NAME, server.session);
    }

    @Override
    public void load() {
        final var nbtCompound = ModPathUtils.readNbt(MANAGERS_FILE_NAME, server.session);

        final var keys = nbtCompound.getKeys();
        notEmpty = !keys.isEmpty();
        for (String key : keys) {
            try {
                final var managerNbt = nbtCompound.getCompound(key);
                final var masterPlayerId = UUID.fromString(key);
                final var manager = new ServerFortressManager(server);
                manager.readFromNbt(managerNbt);

                serverManagers.put(masterPlayerId, manager);
            } catch (RuntimeException exp) {
                ModLogger.LOGGER.warn("Failed to load server manager for player with id " + key, exp);
            }
        }
    }

    @Override
    public boolean isNotEmpty() {
        if (notEmpty == null) {
            throw new IllegalStateException("Server managers not loaded yet");
        }
        return notEmpty;
    }

    public Optional<IServerManagersProvider> findReachableFortress(BlockPos pos, double reachRange) {
        for (ServerFortressManager manager : serverManagers.values()) {
            final var fortressCenter = manager.getFortressCenter();
            if(fortressCenter == null) continue;
            final var villageRadius = manager.getVillageRadius();

            if(fortressCenter.isWithinDistance(pos, villageRadius + reachRange)) {
                return Optional.of(manager);
            }
        }
        return Optional.empty();
    }
}
