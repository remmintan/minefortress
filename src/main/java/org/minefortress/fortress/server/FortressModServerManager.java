package org.minefortress.fortress.server;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.minefortress.data.FortressModDataLoader;
import org.minefortress.fortress.FortressServerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FortressModServerManager {

    private static final String MANAGERS_FILE_NAME = "serverManagers.nbt";
    private final MinecraftServer server;
    private final Map<UUID, FortressServerManager> serverManagers = new HashMap<>();

    public FortressModServerManager(MinecraftServer server) {
        this.server = server;
    }

    public FortressServerManager getByPlayer(ServerPlayerEntity player) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return null;
        }
        final var playerId = player.getUuid();
        return serverManagers.computeIfAbsent(playerId, (it) -> new FortressServerManager(server));
    }

    public FortressServerManager getByFortressId(UUID uuid) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return null;
        }
        for(FortressServerManager manager : serverManagers.values()) {
            if(manager.getId().equals(uuid)) {
                return manager;
            }
        }
        LogManager.getLogger().warn("Can't find fortress with id " + uuid + " creating new one");
        final var fortressServerManager = new FortressServerManager(server);
        fortressServerManager.setId(uuid);
        serverManagers.put(UUID.randomUUID(), fortressServerManager);
        return fortressServerManager;
    }

    public Optional<ServerPlayerEntity> getPlayerByFortressId(UUID fortressId) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return null;
        }
        for (Map.Entry<UUID, FortressServerManager> entry : serverManagers.entrySet()) {
            if (entry.getValue().getId().equals(fortressId)) {
                return Optional.ofNullable(server.getOverworld().getPlayerByUuid(entry.getKey())).map(ServerPlayerEntity.class::cast);
            }
        }
        return Optional.empty();
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

    public void tick(PlayerManager playerManager) {
        for (Map.Entry<UUID, FortressServerManager> entry : serverManagers.entrySet()) {
            final var playerId = entry.getKey();
            final var manager = entry.getValue();
            final var player = playerManager.getPlayer(playerId);
            manager.tick(player, server);
        }
    }

    public void load() {
        final var nbtCompound = FortressModDataLoader.readNbt(MANAGERS_FILE_NAME, server.session);
        for (String key : nbtCompound.getKeys()) {
            final var managerNbt = nbtCompound.getCompound(key);
            final var manager = new FortressServerManager(server);
            manager.readFromNbt(managerNbt);
            serverManagers.put(UUID.fromString(key), manager);
        }
    }

}
