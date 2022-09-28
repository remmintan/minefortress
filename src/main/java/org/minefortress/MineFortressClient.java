package org.minefortress;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.world.ClientWorld;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.entity.renderer.ColonistRenderer;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreen;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreen;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.*;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressKeybindings;
import org.minefortress.tasks.ClientTasksHolder;

import static org.minefortress.MineFortressMod.FORTRESS_CRAFTING_SCREEN_HANDLER;
import static org.minefortress.MineFortressMod.FORTRESS_FURNACE_SCREEN_HANDLER;

public class MineFortressClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(FortressEntities.COLONIST_ENTITY_TYPE, ColonistRenderer::new);
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
                    final ClientTasksHolder clientTasksHolder = ((FortressClientWorld) world).getClientTasksHolder();

                    if(client.options.sprintKey.isPressed()) {
                        clientTasksHolder.cancelAllTasks();
                    } else {
                        clientTasksHolder.cancelTask();
                    }
                }
            }
        });

        ScreenRegistry.register(FORTRESS_CRAFTING_SCREEN_HANDLER, FortressCraftingScreen::new);
        ScreenRegistry.register(FORTRESS_FURNACE_SCREEN_HANDLER, FortressFurnaceScreen::new);

        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FINISH_TASK, ClientboundTaskExecutedPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_MANAGER_SYNC, ClientboundSyncFortressManagerPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SELECT_COLONIST, ClientboundFollowColonistPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_ADD_BLUEPRINT, ClientboundAddBlueprintPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_UPDATE_BLUEPRINT, ClientboundUpdateBlueprintPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_RESET_BLUEPRINT, ClientboundResetBlueprintPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_BUILDINGS_SYNC, ClientboundSyncBuildingsPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SPECIAL_BLOCKS_SYNC, ClientboundSyncSpecialBlocksPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_SYNC, ClientboundProfessionSyncPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_RESOURCES_SYNC, ClientboundSyncItemsPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_COMBAT_STATE_SYNC, ClientboundSyncCombatStatePacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_OPEN_BLUEPRINTS_FOLDER, ClientboundOpenBlueprintsFolderPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_INIT, ClientboundProfessionsInitPacket::new);
    }
}
