package org.minefortress.registries.events;


import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BlueprintsDimensionUtilsKt;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import org.minefortress.MineFortressMod;
import org.minefortress.interfaces.FortressWorldCreator;
import org.minefortress.utils.ModUtils;

public class FortressServerEvents {

    public static void register() {
        EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> {
            if(ModUtils.isFortressGamemode(entity)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, pos, dir) -> {
            if(ModUtils.isFortressGamemode(entity)) {
                final var rotationVector = entity.getRotationVector();
                return Direction.getFacing(rotationVector.x, rotationVector.y, rotationVector.z);
            }
            return dir;
        });

        EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register((player, pos, vanilla) -> {
            if(ModUtils.isFortressGamemode(player)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        // initialising the fortress server on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final var fortressServer = (IFortressServer) server;
            final var player = handler.player;
            final var fortressModServerManager = fortressServer.get_FortressModServerManager();
            final var manager = fortressModServerManager.getFortressManager(player);
            final var provider = fortressModServerManager.getManagersProvider(player);
            manager.syncOnJoin(fortressModServerManager.isCampfireEnabled(), fortressModServerManager.isBorderEnabled());
            final var serverProfessionManager = provider.getProfessionsManager();
            serverProfessionManager.sendProfessions(player);
            serverProfessionManager.scheduleSync();

            if(player instanceof FortressServerPlayerEntity fortressPlayer) {
                if(fortressPlayer.was_InBlueprintWorldWhenLoggedOut() && fortressPlayer.get_PersistedPos() != null) {
                    final var pos = fortressPlayer.get_PersistedPos();
                    player.teleport(pos.x, pos.y, pos.z);
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            final var inBlueprintWorldOnDisconnect = handler.player.getWorld().getRegistryKey() == BlueprintsDimensionUtilsKt.getBLUEPRINT_DIMENSION_KEY();
            if(handler.player instanceof FortressServerPlayerEntity fortressPlayer) {
                fortressPlayer.set_WasInBlueprintWorldWhenLoggedOut(inBlueprintWorldOnDisconnect);
            }
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if(server instanceof IFortressServer IFortressServer) {
                final var saveProps = server.getSaveProperties();
                if(saveProps instanceof FortressWorldCreator wcProps) {
                    IFortressServer.get_FortressModServerManager().load(wcProps.is_ShowCampfire(), wcProps.is_BorderEnabled());
                } else {
                    IFortressServer.get_FortressModServerManager().load();
                }
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if(server instanceof IFortressServer aserver) {
                aserver.get_FortressModServerManager().save();
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if(server instanceof IFortressServer IFortressServer) {
                IFortressServer.get_FortressModServerManager().tick(server.getPlayerManager());
            }
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (destination.getRegistryKey() == BlueprintsDimensionUtilsKt.getBLUEPRINT_DIMENSION_KEY()) {
                player.changeGameMode(GameMode.CREATIVE);
            } else if (origin.getRegistryKey() == BlueprintsDimensionUtilsKt.getBLUEPRINT_DIMENSION_KEY()) {
                player.changeGameMode(MineFortressMod.FORTRESS);
            }
        });

        PlayerBlockEventsKt.registerPlayerBlockEvents();
    }

}
