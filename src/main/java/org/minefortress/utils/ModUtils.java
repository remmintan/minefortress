package org.minefortress.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import org.minefortress.MineFortressMod;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.registries.FortressKeybindings;

import java.nio.file.Path;

@MethodsReturnNonnullByDefault
public class ModUtils {

    public static IFortressMinecraftClient getFortressClient() {
        return (IFortressMinecraftClient) MinecraftClient.getInstance();
    }

    public static Path getBlueprintsFolder() {
        return FabricLoader.getInstance()
                .getGameDir()
                .resolve(MineFortressMod.BLUEPRINTS_FOLDER_NAME);
    }


    public static boolean shouldReleaseCamera() {
        final var client = MinecraftClient.getInstance();
        final var options = client.options;
        return options.pickItemKey.isPressed() || FortressKeybindings.releaseCameraKeybinding.isPressed();
    }

}
