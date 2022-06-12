package org.minefortress.data;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class FortressModDataLoader {

    private static final Object key = new Object();
    private static volatile FortressModDataLoader instance;

    public static FortressModDataLoader getInstance() {
        if(instance == null) {
            synchronized (key) {
                if(instance == null) {
                    instance = new FortressModDataLoader();
                }
            }
        }

        return instance;
    }

    private static final String MOD_DIR = "minefortress";
    private static final String WORLD_DIR_PREFIX = "blueprints";

    private final LevelStorage fortressLevelStorage;

    private FortressModDataLoader() {
        this.fortressLevelStorage = LevelStorage.create(getModDir());
    }

    public LevelStorage.Session getBlueprintsWorldSession() {
        try {
            return fortressLevelStorage.createSession(WORLD_DIR_PREFIX+ UUID.randomUUID());
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveNbt(NbtCompound nbt, String fileName) {
        final var file = getModDir().resolve(fileName).toFile();
        try {
            NbtIo.writeCompressed(nbt, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NbtCompound readNbt(String fileName) {
        final var file = getModDir().resolve(fileName).toFile();
        if(file.exists()) {
            try {
                return NbtIo.readCompressed(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new NbtCompound();
        }
    }

    private Path getModDir() {
        return FabricLoader.getInstance().getGameDir().resolve(MOD_DIR);
    }

}
