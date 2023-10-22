package org.minefortress.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.world.GameMode;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTasksHolder;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder;
import org.jetbrains.annotations.NotNull;
import org.minefortress.MineFortressMod;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import org.minefortress.fight.influence.ClientInfluenceManager;
import org.minefortress.fortress.ClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreasClientManager;
import org.minefortress.interfaces.IFortressMinecraftClient;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import org.minefortress.tasks.ClientVisualTasksHolder;

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

    public static boolean isFortressGamemode(GameMode gameMode) {
        return gameMode == MineFortressMod.FORTRESS;
    }

    public static IFortressMinecraftClient getFortressClient() {
        return (IFortressMinecraftClient) MinecraftClient.getInstance();
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

    public static ClientFortressManager getFortressClientManager() {
        return getFortressClient().get_FortressClientManager();
    }

    public static IClientProfessionManager getProfessionManager() {
        return getFortressClientManager().getProfessionManager();
    }

    public static IClientBlueprintManager getBlueprintManager() {
        return getFortressClient().get_BlueprintManager();
    }
    public static ClientInfluenceManager getInfluenceManager() {
        return getFortressClient().get_InfluenceManager();
    }

    public static ISelectionManager getSelectionManager() {
        return getFortressClient().get_SelectionManager();
    }

    public static Optional<ITasksInformationHolder> getFortressClientWorld() {
        return Optional.ofNullable(MinecraftClient.getInstance())
                .map(it -> it.world)
                .map(ITasksInformationHolder.class::cast);
    }

    public static Optional<IClientTasksHolder> getClientTasksHolder() {
        return getFortressClientWorld().map(ITasksInformationHolder::get_ClientTasksHolder);
    }

    @NotNull
    public static ClientPlayerEntity getClientPlayer() {
        return Objects.requireNonNull(MinecraftClient.getInstance().player);
    }

    public static IAreasClientManager getAreasClientManager() {
        return getFortressClient().get_AreasClientManager();
    }


    public static boolean shouldReleaseCamera() {
        final var client = MinecraftClient.getInstance();
        final var options = client.options;


        final var blueprintManager = getBlueprintManager();
        final var selectionManager = getSelectionManager();
        final var areasClientManager = getAreasClientManager();
        final var influenceManager = getInfluenceManager();

        final var anyManagerSelecting = blueprintManager.isSelecting() ||
                selectionManager.isSelecting() ||
                areasClientManager.isSelecting() ||
                influenceManager.isSelecting();

        return options.pickItemKey.isPressed() || (options.sprintKey.isPressed() && !anyManagerSelecting);
    }

}
