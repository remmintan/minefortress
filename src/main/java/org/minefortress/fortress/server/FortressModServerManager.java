package org.minefortress.fortress.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.data.FortressModDataLoader;
import org.minefortress.fortress.FortressServerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FortressModServerManager {

    private static final String MANAGERS_FILE_NAME = "server-managers.nbt";
    private final MinecraftServer server;
    private final Map<UUID, FortressServerManager> serverManagers = new HashMap<>();

    public FortressModServerManager(MinecraftServer server) {
        this.server = server;
    }

    public FortressServerManager getByPlayer(ServerPlayerEntity player) {
        final var playerId = player.getUuid();
        return serverManagers.computeIfAbsent(playerId, (it) -> new FortressServerManager(server));
    }

    public FortressServerManager getByPlayerId(UUID uuid) {
        if(serverManagers.containsKey(uuid)) {
            return serverManagers.get(uuid);
        } else {
            throw new IllegalArgumentException("No server manager found for player with id " + uuid);
        }
    }

    public void tick(PlayerManager playerManager) {
        for (Map.Entry<UUID, FortressServerManager> entry : serverManagers.entrySet()) {
            final var playerId = entry.getKey();
            final var manager = entry.getValue();
            final var player = playerManager.getPlayer(playerId);
            manager.tick(player);
        }
    }

    public void save() {
        final var nbt = new NbtCompound();
        for (Map.Entry<UUID, FortressServerManager> entry : serverManagers.entrySet()) {
            final var fortressNbt = new NbtCompound();
            final var id = entry.getKey();
            final var manager = entry.getValue();
            manager.writeToNbt(fortressNbt);
            nbt.put(id.toString(), fortressNbt);
        }

        FortressModDataLoader.saveNbt(nbt, MANAGERS_FILE_NAME, server.session);
    }

    public void load() {
        final var nbtCompound = FortressModDataLoader.readNbt(MANAGERS_FILE_NAME, server.session);
        for (String key : nbtCompound.getKeys()) {
            final var managerNbt = nbtCompound.getCompound(key);
            final var masterPlayerId = UUID.fromString(key);
            final var manager = new FortressServerManager(server);
            manager.readFromNbt(managerNbt);

            serverManagers.put(masterPlayerId, manager);
        }
    }

}
