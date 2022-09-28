package org.minefortress.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.nfd.NativeFileDialog.*;


public class ModOSUtils {

    public static Optional<String> showSaveDialog(String fileTypes, @Nullable String defaultPath) {
        final var pathPointer = memAllocPointer(1);
        try {
            final var result = NFD_SaveDialog(fileTypes, defaultPath, pathPointer);
            if (result == NFD_OKAY) {
                final var pathString = pathPointer.getStringUTF8(0);
                nNFD_Free(pathPointer.get(0));
                return Optional.of(pathString);
            } else if (result == NFD_CANCEL) {
                return Optional.empty();
            } else {
                throw new RuntimeException("Error: " + NFD_GetError());
            }
        } finally {
            memFree(pathPointer);
        }
    }

    public static Optional<String> showOpenDialog(String fileTypes, @Nullable String defaultPath) {
        final var pathPointer = memAllocPointer(1);
        try {
            final var result = NFD_OpenDialog(fileTypes, defaultPath, pathPointer);
            if (result == NFD_OKAY) {
                final var pathString = pathPointer.getStringUTF8(0);
                nNFD_Free(pathPointer.get(0));
                return Optional.of(pathString);
            } else if (result == NFD_CANCEL) {
                return Optional.empty();
            } else {
                throw new RuntimeException("Error: " + NFD_GetError());
            }
        } finally {
            memFree(pathPointer);
        }
    }

}
