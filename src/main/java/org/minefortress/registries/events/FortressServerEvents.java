package org.minefortress.registries.events;


import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.IFortressModVersionHolder;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.S2CSyncGamemodePacket;

public class FortressServerEvents {

    public static void register() {
        // initialising the fortress server on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (server.getSaveProperties() instanceof IFortressModVersionHolder holder && holder.is_OutdatedVersion()) {
                handler.disconnect(Text.of("Outdated world version"));
                return;
            }

            final var player = handler.player;
            syncTheFortressGamemode((IFortressServer) server, player);

            if (ServerModUtils.hasFortress(player)) {
                final var manager = ServerModUtils.getFortressManager(player);
                final var provider = ServerModUtils.getManagersProvider(player);
                provider.sync();
                manager.sync();
                final var serverProfessionManager = provider.getProfessionsManager();
                serverProfessionManager.sendProfessions(player);
                serverProfessionManager.sync();
            }
        });

        PlayerSleepEvents.INSTANCE.register();
        BlueprintWorldEvents.INSTANCE.register();
        PlayerBlockEvents.INSTANCE.register();
    }

    private static void syncTheFortressGamemode(IFortressServer server, ServerPlayerEntity player) {
        final var fortressGamemode = server.get_FortressGamemode();
        final var packet = new S2CSyncGamemodePacket(fortressGamemode);
        FortressServerNetworkHelper.send(player, S2CSyncGamemodePacket.CHANNEL, packet);
    }

}
