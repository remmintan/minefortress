package org.minefortress.registries.events;


import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.IFortressModVersionHolder;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import net.remmintan.mods.minefortress.core.services.PatronStatusService;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.S2CStartFortressConfiguration;
import net.remmintan.mods.minefortress.networking.s2c.S2CSyncGamemodePacket;
import org.minefortress.MineFortressMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FortressServerEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(FortressServerEvents.class);

    public static void register() {
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            setSupporterStatus(server, handler.player);
        });
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
                                it -> {
                                    it.sync();
                                    if (!it.isPawnsSkinSet()) {
                                        final var packet = new S2CStartFortressConfiguration();
                                        FortressServerNetworkHelper.send(player, S2CStartFortressConfiguration.CHANNEL, packet);
                                    }
                                },
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

            if (player instanceof final IFortressServerPlayerEntity fortressPlayer) {
                MineFortressMod.getScheduledExecutor().schedule(() -> server.execute(() -> {
                    if (!fortressPlayer.is_ModVersionValidated()) {
                        player.networkHandler.disconnect(Text.of("Couldn't validate mod version"));
                    }
                }), 5, TimeUnit.SECONDS);
            }
        });

        PlayerSleepEvents.INSTANCE.register();
        BlueprintWorldEvents.INSTANCE.register();
        PlayerBlockEvents.INSTANCE.register();
    }

    private static void setSupporterStatus(MinecraftServer server, ServerPlayerEntity player) {
        final var profile = player.getGameProfile();
        final var name = profile.getName();
        final var supporterStatus = PatronStatusService.INSTANCE.getSupporterStatus(name);
        server.execute(() -> {
            if (player instanceof IFortressPlayerEntity playerEntity) {
                playerEntity.set_SupportLevel(supporterStatus);
            }
        });
    }

    private static void syncTheFortressGamemode(IFortressServer server, ServerPlayerEntity player) {
        final var fortressGamemode = server.get_FortressGamemode();
        final var packet = new S2CSyncGamemodePacket(fortressGamemode);
        FortressServerNetworkHelper.send(player, S2CSyncGamemodePacket.CHANNEL, packet);
    }

}
