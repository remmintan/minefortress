package org.minefortress;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.remmintan.mods.minefortress.gui.FortressHandledScreensKt;
import net.remmintan.mods.minefortress.networking.registries.ClientNetworkReceivers;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreen;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreen;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressKeybindings;
import org.minefortress.registries.events.FortressClientEvents;

import static org.minefortress.MineFortressMod.FORTRESS_CRAFTING_SCREEN_HANDLER;
import static org.minefortress.MineFortressMod.FORTRESS_FURNACE_SCREEN_HANDLER;

public class MineFortressClient implements ClientModInitializer {

    public static final GameModeSelectionScreen.GameModeSelection FORTRESS_SELECTION = ClassTinkerers.getEnum(GameModeSelectionScreen.GameModeSelection.class, "FORTRESS");

    @Override
    public void onInitializeClient() {
        FortressKeybindings.init();
        FortressEntities.registerRenderers();
        NetworkReaders.register();

        ScreenRegistry.register(FORTRESS_CRAFTING_SCREEN_HANDLER, FortressCraftingScreen::new);
        ScreenRegistry.register(FORTRESS_FURNACE_SCREEN_HANDLER, FortressFurnaceScreen::new);

        ClientNetworkReceivers.registerReceivers();
        FortressClientEvents.register();
        FortressHandledScreensKt.registerScreens();
    }
}
