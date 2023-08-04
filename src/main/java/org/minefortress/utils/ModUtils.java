package org.minefortress.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.minefortress.MineFortressMod;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.fight.influence.ClientInfluenceManager;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.automation.areas.AreasClientManager;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.professions.ClientProfessionManager;
import org.minefortress.selections.SelectionManager;
import org.minefortress.tasks.ClientVisualTasksHolder;
import Z;
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

    public static FortressMinecraftClient getFortressClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
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

    public static FortressClientManager getFortressClientManager() {
        return getFortressClient().getFortressClientManager();
    }

    public static ClientProfessionManager getProfessionManager() {
        return getFortressClientManager().getProfessionManager();
    }

    public static ClientBlueprintManager getBlueprintManager() {
        return getFortressClient().getBlueprintManager();
    }
    public static ClientInfluenceManager getInfluenceManager() {
        return getFortressClient().getInfluenceManager();
    }

    public static SelectionManager getSelectionManager() {
        return getFortressClient().getSelectionManager();
    }

    public static Optional<FortressClientWorld> getFortressClientWorld() {
        return Optional.ofNullable(MinecraftClient.getInstance())
                .map(it -> it.world)
                .map(FortressClientWorld.class::cast);
    }

    public static Optional<ClientVisualTasksHolder> getClientTasksHolder() {
        return getFortressClientWorld().map(FortressClientWorld::getClientTasksHolder);
    }

    @NotNull
    public static ClientPlayerEntity getClientPlayer() {
        return Objects.requireNonNull(MinecraftClient.getInstance().player);
    }

    public static AreasClientManager getAreasClientManager() {
        return getFortressClient().getAreasClientManager();
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
