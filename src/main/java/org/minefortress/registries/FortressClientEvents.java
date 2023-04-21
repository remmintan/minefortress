package org.minefortress.registries;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.world.ClientWorld;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.ChooseModeScreen;
import org.minefortress.tasks.ClientVisualTasksHolder;
import org.minefortress.utils.ModUtils;

public class FortressClientEvents {

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ModUtils.getFortressClientManager().reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ModUtils.getFortressClientManager().reset());
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            final var clientInFortressGamemode = ModUtils.isClientInFortressGamemode();
            final var middleMouseButtonPressed = client.options.pickItemKey.isPressed();
            final var fortressClient = ModUtils.getFortressClient();
            final var fortressCameraManager = fortressClient.getFortressCameraManager();
            final var player = client.player;
            if(clientInFortressGamemode) {
                final var mouse = client.mouse;
                if(middleMouseButtonPressed) {
                    if(!mouse.isCursorLocked())
                        mouse.lockCursor();
                } else {
                    if(mouse.isCursorLocked())
                        mouse.unlockCursor();
                }


                if(!middleMouseButtonPressed && player != null) {
                    fortressCameraManager.updateCameraPosition();
                }
            }

            final var world = client.world;
            final var notInBlueprintsWorld = world != null && world.getRegistryKey() != BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY;
            if ((middleMouseButtonPressed || !clientInFortressGamemode) && notInBlueprintsWorld) {
                if(player != null) {
                    fortressCameraManager.setRot(player.getPitch(), player.getYaw());
                }
            }

            fortressClient.getFortressHud().tick();
            final var fortressClientManager = fortressClient.getFortressClientManager();
            fortressClientManager.tick(fortressClient);
            if(fortressClientManager.gamemodeNeedsInitialization() && !(client.currentScreen instanceof ChooseModeScreen)) {
                client.setScreen(new ChooseModeScreen());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (FortressKeybindings.switchSelectionKeybinding.wasPressed()) {
                final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
                final ClientBlueprintManager clientBlueprintManager = fortressClient.getBlueprintManager();
                if(clientBlueprintManager.isSelecting()) {
                    fortressClient.getBlueprintManager().selectNext();
                } else {
                    fortressClient.getSelectionManager().toggleSelectionType();
                }
            }

            while (FortressKeybindings.cancelTaskKeybinding.wasPressed()) {
                final ClientWorld world = client.world;
                if(world != null) {
                    final ClientVisualTasksHolder clientVisualTasksHolder = ((FortressClientWorld) world).getClientTasksHolder();

                    if(client.options.sprintKey.isPressed()) {
                        clientVisualTasksHolder.cancelAllTasks();
                    } else {
                        clientVisualTasksHolder.cancelTask();
                    }
                }
            }
        });
    }

}
