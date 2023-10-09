package org.minefortress.registries;


import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemGroups;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.interfaces.ITasksInformationHolder;
import org.minefortress.interfaces.FortressMinecraftClient;
import net.remmintan.mods.minefortress.networking.c2s.C2SRequestResourcesRefresh;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.ChooseModeScreen;
import org.minefortress.tasks.ClientVisualTasksHolder;
import org.minefortress.utils.ModUtils;

public class FortressClientEvents {

    public static void register() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ModUtils.getFortressClientManager().reset());
        ClientTickEvents.START_CLIENT_TICK.register(FortressClientEvents::startClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(FortressClientEvents::endClientTick);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            ItemGroups.updateDisplayContext(handler.getEnabledFeatures(), false, client.world.getRegistryManager());
            final var packet = new C2SRequestResourcesRefresh();
            FortressClientNetworkHelper.send(C2SRequestResourcesRefresh.CHANNEL, packet);
        }));
    }

    private static void startClientTick(MinecraftClient client) {
        final var clientInFortressGamemode = ModUtils.isClientInFortressGamemode();
        final var fortressClient = ModUtils.getFortressClient();
        if(clientInFortressGamemode) {
            final var mouse = client.mouse;
            if(ModUtils.shouldReleaseCamera()) {
                if(!mouse.isCursorLocked())
                    mouse.lockCursor();
            } else {
                if(mouse.isCursorLocked())
                    mouse.unlockCursor();
            }
        }

        fortressClient.get_FortressHud().tick();
        final var fortressClientManager = fortressClient.get_FortressClientManager();
        fortressClientManager.tick(fortressClient);
        if(fortressClientManager.gamemodeNeedsInitialization() && !(client.currentScreen instanceof ChooseModeScreen)) {
            client.setScreen(new ChooseModeScreen());
        }
    }

    private static void endClientTick(MinecraftClient client) {
        while (FortressKeybindings.switchSelectionKeybinding.wasPressed()) {
            final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
            final ClientBlueprintManager clientBlueprintManager = fortressClient.get_BlueprintManager();
            if(clientBlueprintManager.isSelecting()) {
                fortressClient.get_BlueprintManager().selectNext();
            } else {
                fortressClient.get_SelectionManager().toggleSelectionType();
            }
        }

        while (FortressKeybindings.cancelTaskKeybinding.wasPressed()) {
            final ClientWorld world = client.world;
            if(world != null) {
                final ClientVisualTasksHolder clientVisualTasksHolder = ((ITasksInformationHolder) world).get_ClientTasksHolder();

                if(client.options.sprintKey.isPressed()) {
                    clientVisualTasksHolder.cancelAllTasks();
                } else {
                    clientVisualTasksHolder.cancelTask();
                }
            }
        }
    }

}
