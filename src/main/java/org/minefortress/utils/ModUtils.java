package org.minefortress.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.minefortress.MineFortressMod;
import org.minefortress.interfaces.IFortressMinecraftClient;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

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

    public static UUID getCurrentPlayerUUID() {
        return Optional
                .ofNullable(MinecraftClient.getInstance().player)
                .map(ClientPlayerEntity::getUuid)
                .orElseThrow(() -> new IllegalStateException("Player is null"));
    }


    public static boolean shouldReleaseCamera() {
        final var client = MinecraftClient.getInstance();
        final var options = client.options;

        final var blueprintManager = CoreModUtils.getBlueprintManager();
        final var selectionManager = CoreModUtils.getSelectionManager();
        final var areasClientManager = CoreModUtils.getAreasClientManager();

        final var anyManagerSelecting = blueprintManager.isSelecting() ||
                selectionManager.isSelecting() ||
                areasClientManager.isSelecting();

        return options.pickItemKey.isPressed() || (options.sprintKey.isPressed() && !anyManagerSelecting);
    }

}
