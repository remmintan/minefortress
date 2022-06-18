package org.minefortress.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.impl.resource.loader.FabricModResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.knot.FabricGlobalPropertyService;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.File;
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

    public static void saveNbt(NbtCompound nbt, String fileName, LevelStorage.Session session) {
        final var file = getWorldSaveDir(session).resolve(fileName).toFile();
        try {
            if(!file.exists()) file.createNewFile();
            NbtIo.writeCompressed(nbt, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound readNbt(String fileName, LevelStorage.Session session) {
        final var file = getWorldSaveDir(session).resolve(fileName).toFile();
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

    private static Path getWorldSaveDir(LevelStorage.Session session) {
        final var worldDirectory = session.getWorldDirectory(World.OVERWORLD);
        final var modDirectory = worldDirectory.resolve(MOD_DIR);
        if(!modDirectory.toFile().exists()) modDirectory.toFile().mkdir();

        return modDirectory;
    }

    private static Path getModDir() {
//        FabricLauncherBase.getLauncher().getMappingConfiguration()
        return FabricLoader.getInstance().getGameDir().resolve(MOD_DIR);
    }

}
