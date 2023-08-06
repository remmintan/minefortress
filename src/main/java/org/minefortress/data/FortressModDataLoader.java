package org.minefortress.data;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    public static final String MOD_DIR = "minefortress";
    private static final String WORLD_DIR_PREFIX = "blueprints-customization-world";

    private final LevelStorage fortressLevelStorage;

    private FortressModDataLoader() {
        this.fortressLevelStorage = LevelStorage.create(getModDir());
    }

    public LevelStorage.Session getBlueprintsWorldSession() {
        try {
            return fortressLevelStorage.createSessionWithoutSymlinkCheck(WORLD_DIR_PREFIX);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean exists(String folderName, LevelStorage.Session session) {
        return Files.exists(getWorldSaveDir(session).resolve(folderName));
    }

    public static List<NbtCompound> readAllTags(String folderName, LevelStorage.Session session, Collection<String> excludedFiles) {
        final var files = Optional.ofNullable(
                getWorldSaveDir(session)
                .resolve(folderName)
                .toFile()
                .listFiles()
        ).orElse(new File[]{});
        return Arrays.stream(files)
                .filter(it -> !it.isDirectory() && it.getAbsolutePath().endsWith(".nbt") && !excludedFiles.contains(it.getName()))
                .map(FortressModDataLoader::readNbt)
                .toList();
    }

    public static void clearFolder(String folderName, LevelStorage.Session session) {
        final var folder = getWorldSaveDir(session).resolve(folderName).toFile();
        if(folder.exists()) {
            final var files = Optional.ofNullable(folder.listFiles()).orElse(new File[]{});
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static void createFolder(String folderName, LevelStorage.Session session) {
        final var folder = getWorldSaveDir(session).resolve(folderName).toFile();
        if(!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void writeAllTags(Map<String, NbtCompound> tags, LevelStorage.Session session) {
        tags.forEach((k, v) -> saveNbt(v, k, session));
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
        return readNbt(file);
    }

    private static NbtCompound readNbt(File file) {
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

    public static String getFolderAbsolutePath(String folder, LevelStorage.Session session) {
        return getWorldSaveDir(session).resolve(folder).toAbsolutePath().toString();
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
