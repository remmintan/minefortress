package org.minefortress.registries.events;


import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.IFortressModVersionHolder;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.S2CSyncGamemodePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FortressServerEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(FortressServerEvents.class);

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (server.getSaveProperties() instanceof IFortressModVersionHolder holder && holder.is_OutdatedVersion()) {
                handler.disconnect(Text.of("Outdated world version"));
                return;
            }

            final var player = handler.player;
            syncTheFortressGamemode((IFortressServer) server, player);

            if (ServerModUtils.hasFortress(player)) {
                ServerModUtils
                        .getFortressManager(player)
                        .ifPresentOrElse(
                                IServerFortressManager::sync,
                                () -> LOGGER.warn("Can't find the fortress block while the fortress is set up!")
                        );
                ServerModUtils
                        .getManagersProvider(player)
                        .ifPresentOrElse(
                                provider -> {
                                    provider.sync();
                                    final var serverProfessionManager = provider.getProfessionsManager();
                                    serverProfessionManager.sendProfessions(player);
                                    serverProfessionManager.sync();
                                },
                                () -> LOGGER.warn("Can't find the fortress block while the fortress is set up!")
                        );

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
