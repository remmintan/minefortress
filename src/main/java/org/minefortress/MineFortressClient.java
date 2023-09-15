package org.minefortress;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreen;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreen;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.registries.FortressClientEvents;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressKeybindings;

import static org.minefortress.MineFortressMod.FORTRESS_CRAFTING_SCREEN_HANDLER;
import static org.minefortress.MineFortressMod.FORTRESS_FURNACE_SCREEN_HANDLER;

public class MineFortressClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FortressKeybindings.init();
        FortressEntities.registerRenderers();

        ScreenRegistry.register(FORTRESS_CRAFTING_SCREEN_HANDLER, FortressCraftingScreen::new);
        ScreenRegistry.register(FORTRESS_FURNACE_SCREEN_HANDLER, FortressFurnaceScreen::new);

        FortressClientNetworkHelper.registerReceivers();
        FortressClientEvents.register();
    }
}
