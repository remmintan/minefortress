package org.minefortress;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.world.ClientWorld;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreen;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreen;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressEvents;
import org.minefortress.registries.FortressKeybindings;
import org.minefortress.tasks.ClientVisualTasksHolder;

import static org.minefortress.MineFortressMod.FORTRESS_CRAFTING_SCREEN_HANDLER;
import static org.minefortress.MineFortressMod.FORTRESS_FURNACE_SCREEN_HANDLER;

public class MineFortressClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FortressEntities.registerRenderers();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (FortressKeybindings.switchSelectionKeybinding.wasPressed()) {
                final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
                final ClientBlueprintManager clientBlueprintManager = fortressClient.getBlueprintManager();
                if(clientBlueprintManager.hasSelectedBlueprint()) {
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

        ScreenRegistry.register(FORTRESS_CRAFTING_SCREEN_HANDLER, FortressCraftingScreen::new);
        ScreenRegistry.register(FORTRESS_FURNACE_SCREEN_HANDLER, FortressFurnaceScreen::new);

        FortressClientNetworkHelper.registerReceivers();

        FortressEvents.registerClient();
    }
}
