package org.minefortress.registries;


import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import org.minefortress.blueprints.world.BlueprintsWorld;
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

        AttackEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
            if(ModUtils.isFortressGamemode(player)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
            if(ModUtils.isFortressGamemode(player)) {
                return ActionResult.FAIL;
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
            final var inBlueprintWorldOnDisconnect = handler.player.getWorld().getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY;
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
                aserver.get_BlueprintsWorld().closeSession();
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if(server instanceof IFortressServer IFortressServer) {
                IFortressServer.get_FortressModServerManager().tick(server.getPlayerManager());
            }
        });
    }

}
