package org.minefortress;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.world.ClientWorld;
import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.entity.renderer.ColonistRenderer;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.ClientboundSyncFortressManagerPacket;
import org.minefortress.network.ClientboundTaskExecutedPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressKeybindings;
import org.minefortress.tasks.ClientTasksHolder;

public class MineFortressClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(FortressEntities.COLONIST_ENTITY_TYPE, ColonistRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (FortressKeybindings.switchSelectionKeybinding.wasPressed()) {
                final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
                final BlueprintManager blueprintManager = fortressClient.getBlueprintManager();
                if(blueprintManager.hasSelectedBlueprint()) {
                    fortressClient.getBlueprintMetadataManager().selectNext();
                } else {
                    fortressClient.getSelectionManager().toggleSelectionType();
                }
            }

            while (FortressKeybindings.cancelTaskKeybinding.wasPressed()) {
                final ClientWorld world = client.world;
                if(world != null) {
                    final ClientTasksHolder clientTasksHolder = ((FortressClientWorld) world).getClientTasksHolder();

                    if(client.options.keySprint.isPressed()) {
                        clientTasksHolder.cancelAllTasks();
                    } else {
                        clientTasksHolder.cancelTask();
                    }
                }
            }
        });

        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FINISH_TASK, ClientboundTaskExecutedPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_MANAGER_SYNC, ClientboundSyncFortressManagerPacket::new);
    }
}
