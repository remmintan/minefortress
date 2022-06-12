package org.minefortress.fortress.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.minefortress.data.FortressModDataLoader;
import org.minefortress.fortress.FortressServerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FortressModServerManager {

    private static final String MANAGERS_FILE_NAME = "managers.nbt";
    private final MinecraftServer server;

    private Map<UUID, FortressServerManager> managers = new HashMap<>();

    public FortressModServerManager(MinecraftServer server) {
        this.server = server;
    }

    public FortressServerManager getByPlayer(ServerPlayerEntity player) {
        return managers.computeIfAbsent(player.getUuid(), (it) -> new FortressServerManager(server));
    }

    public FortressServerManager getByFortressId(UUID uuid) {
        for(FortressServerManager manager : managers.values()) {
            if(manager.getId().equals(uuid)) {
                return manager;
            }
        }
        LogManager.getLogger().warn("Can't find fortress with id " + uuid + " creating new one");
        return managers.put(UUID.randomUUID(), new FortressServerManager(server));
    }

    public void save() {
        final var nbt = new NbtCompound();
        for (Map.Entry<UUID, FortressServerManager> entry : managers.entrySet()) {
            final var fortressNbt = new NbtCompound();
            final var id = entry.getKey();
            final var manager = entry.getValue();
            manager.writeToNbt(fortressNbt);
            nbt.put(id.toString(), fortressNbt);
        }

        FortressModDataLoader.getInstance().saveNbt(nbt, MANAGERS_FILE_NAME);
    }

    public void tick(PlayerManager playerManager) {
        for (Map.Entry<UUID, FortressServerManager> entry : managers.entrySet()) {
            final var playerId = entry.getKey();
            final var manager = entry.getValue();
            final var player = playerManager.getPlayer(playerId);
        }
    }

    public void load() {
        final var nbtCompound = FortressModDataLoader.getInstance().readNbt(MANAGERS_FILE_NAME);
        for (String key : nbtCompound.getKeys()) {
            final var managerNbt = nbtCompound.getCompound(key);
            final var manager = new FortressServerManager(server);
            manager.readFromNbt(managerNbt);
            managers.put(UUID.fromString(key), manager);
        }
    }

}
