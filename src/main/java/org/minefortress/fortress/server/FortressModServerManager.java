package org.minefortress.fortress.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import org.minefortress.data.FortressModDataLoader;
import org.minefortress.fortress.FortressServerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FortressModServerManager {

    private static final String MANAGERS_FILE_NAME = "server-managers.nbt";
    private final MinecraftServer server;
    private final Map<UUID, FortressServerManager> serverManagers = new HashMap<>();

    private boolean campfireEnabled;
    private boolean borderEnabled;

    public FortressModServerManager(MinecraftServer server) {
        this.server = server;
    }

    public FortressServerManager getByPlayer(ServerPlayerEntity player) {
        final var playerId = Uuids.getUuidFromProfile(player.getGameProfile());
        return serverManagers.computeIfAbsent(playerId, (it) -> new FortressServerManager(server));
    }

    public FortressServerManager getByPlayerId(UUID uuid) {
        if(serverManagers.containsKey(uuid)) {
            return serverManagers.get(uuid);
        } else {
            // TODO: write some fallback logic to find the player
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

        nbt.putBoolean("campfireEnabled", campfireEnabled);
        nbt.putBoolean("borderEnabled", borderEnabled);

        FortressModDataLoader.saveNbt(nbt, MANAGERS_FILE_NAME, server.session);
    }

    public void load() {
        load(true, true);
    }

    public void load(boolean campfireEnabled, boolean borderEnabled) {
        final var nbtCompound = FortressModDataLoader.readNbt(MANAGERS_FILE_NAME, server.session);

        boolean campfireEnabledSet = false;
        boolean borderEnabledSet = false;

        for (String key : nbtCompound.getKeys()) {
            if(key.equals("campfireEnabled")) {
                this.campfireEnabled = nbtCompound.getBoolean(key);
                campfireEnabledSet = true;
                continue;
            }

            if(key.equals("borderEnabled")) {
                this.borderEnabled = nbtCompound.getBoolean(key);
                borderEnabledSet = true;
                continue;
            }

            final var managerNbt = nbtCompound.getCompound(key);
            final var masterPlayerId = UUID.fromString(key);
            final var manager = new FortressServerManager(server);
            manager.readFromNbt(managerNbt);

            serverManagers.put(masterPlayerId, manager);
        }

        if(!campfireEnabledSet) {
            this.campfireEnabled = campfireEnabled;
        }
        if(!borderEnabledSet) {
            this.borderEnabled = borderEnabled;
        }
    }

    public Optional<FortressServerManager> findReachableFortress(BlockPos pos, double reachRange) {
        for (FortressServerManager manager : serverManagers.values()) {
            final var fortressCenter = manager.getFortressCenter();
            if(fortressCenter == null) continue;
            final var villageRadius = manager.getVillageRadius();

            if(fortressCenter.isWithinDistance(pos, villageRadius + reachRange)) {
                return Optional.of(manager);
            }
        }
        return Optional.empty();
    }

    public boolean isCampfireEnabled() {
        return campfireEnabled;
    }

    public boolean isBorderEnabled() {
        return borderEnabled;
    }
}
