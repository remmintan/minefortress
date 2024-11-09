package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ModPathUtils {

    public static final String MOD_DIR = "minefortress";

    private ModPathUtils() {
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

    private static Path getWorldSaveDir(LevelStorage.Session session) {
        final var worldDirectory = session.getWorldDirectory(World.OVERWORLD);
        final var modDirectory = worldDirectory.resolve(MOD_DIR);
        if(!modDirectory.toFile().exists()) modDirectory.toFile().mkdir();

        return modDirectory;
    }

}
