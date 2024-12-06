package org.minefortress.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IClientBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreasClientManager;
import org.jetbrains.annotations.NotNull;
import org.minefortress.MineFortressMod;
import org.minefortress.interfaces.IFortressMinecraftClient;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@MethodsReturnNonnullByDefault
public class ModUtils {

    public static boolean isFortressGamemode(PlayerEntity player) {
        if(player instanceof ServerPlayerEntity serverPlayer) {
            return serverPlayer.interactionManager.getGameMode() == MineFortressMod.FORTRESS;
        }
        if(player instanceof ClientPlayerEntity) {
            return isClientInFortressGamemode();
        }
        return false;
    }

    public static boolean isFortressGamemode(LivingEntity livingEntity) {
        if(livingEntity instanceof PlayerEntity player) {
            return isFortressGamemode(player);
        }
        return false;
    }

    public static IFortressMinecraftClient getFortressClient() {
        return (IFortressMinecraftClient) MinecraftClient.getInstance();
    }

    private static IClientManagersProvider getClientManagersProvider() {
        return (IClientManagersProvider) MinecraftClient.getInstance();
    }

    public static boolean isClientInFortressGamemode() {
        final var interactionManager = MinecraftClient.getInstance().interactionManager;
        return interactionManager != null && interactionManager.getCurrentGameMode() == MineFortressMod.FORTRESS;
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

    public static IClientFortressManager getFortressClientManager() {
        return getClientManagersProvider().get_ClientFortressManager();
    }

    public static IClientProfessionManager getProfessionManager() {
        return getFortressClientManager().getProfessionManager();
    }

    public static IClientBlueprintManager getBlueprintManager() {
        return getClientManagersProvider().get_BlueprintManager();
    }

    public static IClientBuildingsManager getBuildingsManager() {
        return getClientManagersProvider().get_BuildingsManager();
    }

    public static ISelectionManager getSelectionManager() {
        return getClientManagersProvider().get_SelectionManager();
    }

    @NotNull
    public static ClientPlayerEntity getClientPlayer() {
        return Objects.requireNonNull(MinecraftClient.getInstance().player);
    }

    public static IAreasClientManager getAreasClientManager() {
        return getClientManagersProvider().get_AreasClientManager();
    }


    public static boolean shouldReleaseCamera() {
        final var client = MinecraftClient.getInstance();
        final var options = client.options;

        final var blueprintManager = getBlueprintManager();
        final var selectionManager = getSelectionManager();
        final var areasClientManager = getAreasClientManager();

        final var anyManagerSelecting = blueprintManager.isSelecting() ||
                selectionManager.isSelecting() ||
                areasClientManager.isSelecting();

        return options.pickItemKey.isPressed() || (options.sprintKey.isPressed() && !anyManagerSelecting);
    }

}
