package org.minefortress.registries;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.utils.ModUtils;

public class FortressEvents {

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
            final var fortressServer = (FortressServer) server;
            fortressServer.getFortressModServerManager().getByPlayer(handler.player);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if(server instanceof FortressServer fortressServer) {
                fortressServer.getFortressModServerManager().load();
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if(server instanceof FortressServer fortressServer) {
                fortressServer.getFortressModServerManager().save();
                fortressServer.getBlueprintsWorld().closeSession();
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if(server instanceof FortressServer fortressServer) {
                fortressServer.getFortressModServerManager().tick(server.getPlayerManager());
            }
        });
    }

}
