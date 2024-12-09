package org.minefortress.registries.events;


import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.ActionResult;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.client.IHoveredBlockProvider;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.C2SRequestResourcesRefresh;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.controls.MouseEvents;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.registries.FortressKeybindings;
import org.minefortress.registries.events.client.ToastEvents;
import org.minefortress.renderer.gui.ChooseModeScreen;
import org.minefortress.utils.ModUtils;

public class FortressClientEvents {

    public static void register() {
        new ToastEvents().register();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CoreModUtils.getFortressClientManager().reset());
        ClientTickEvents.START_CLIENT_TICK.register(FortressClientEvents::startClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(FortressClientEvents::endClientTick);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            ItemGroups.updateDisplayContext(handler.getEnabledFeatures(), false, client.world.getRegistryManager());
            final var packet = new C2SRequestResourcesRefresh();
            FortressClientNetworkHelper.send(C2SRequestResourcesRefresh.CHANNEL, packet);
        }));

        UseEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
            if (!FortressGamemodeUtilsKt.isFortressGamemode(player) || !world.isClient()) return ActionResult.PASS;

            if (CoreModUtils.getFortressClientManager().getState() == FortressState.COMBAT) {
                final var provider = CoreModUtils.getMineFortressManagersProvider();
                final var selectionManager = provider.getTargetedSelectionManager();
                final var fightManager = CoreModUtils.getFortressClientManager().getFightManager();

                fightManager.setTarget(entity, selectionManager);
            }

            return ActionResult.FAIL;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
            if (FortressGamemodeUtilsKt.isFortressGamemode(player)) {
                if (entity instanceof IFortressAwareEntity fortressAwareEntity) {
                    final var selectionManager = CoreModUtils.getMineFortressManagersProvider().get_PawnsSelectionManager();
                    selectionManager.selectSingle(fortressAwareEntity);
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    private static void startClientTick(MinecraftClient client) {
        if (!FortressGamemodeUtilsKt.isClientInFortressGamemode()) return;

        MouseEvents.checkMouseStateAndFireEvents();

        final var mouse = client.mouse;
        if(ModUtils.shouldReleaseCamera()) {
            if(!mouse.isCursorLocked())
                mouse.lockCursor();
        } else {
            if(mouse.isCursorLocked())
                mouse.unlockCursor();
        }

        final var fortressClient = (IFortressMinecraftClient) client;
        fortressClient.get_FortressHud().tick();
        final var provider = CoreModUtils.getMineFortressManagersProvider();
        final var fortressClientManager = provider.get_ClientFortressManager();
        fortressClientManager.tick(hoveredBlockProvider());
        if(fortressClientManager.gamemodeNeedsInitialization() && !(client.currentScreen instanceof ChooseModeScreen)) {
            client.setScreen(new ChooseModeScreen());
        }
    }

    private static IHoveredBlockProvider hoveredBlockProvider() {
        return (IHoveredBlockProvider) MinecraftClient.getInstance();
    }

    private static void endClientTick(MinecraftClient client) {
        while (FortressKeybindings.switchSelectionKeybinding.wasPressed()) {
            final var fortressClient = (IClientManagersProvider) client;
            fortressClient.get_SelectionManager().toggleSelectionType();
        }

        while (FortressKeybindings.cancelTaskKeybinding.wasPressed()) {
            final ClientWorld world = client.world;
            if(world != null) {
                final var clientVisualTasksHolder = ((ITasksInformationHolder) world).get_ClientTasksHolder();

                if(client.options.sprintKey.isPressed()) {
                    clientVisualTasksHolder.cancelAllTasks();
                } else {
                    clientVisualTasksHolder.cancelLatestTask();
                }
            }
        }
    }

}
