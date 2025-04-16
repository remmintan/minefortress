package org.minefortress;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.remmintan.mods.minefortress.blocks.FortressBlocks;
import net.remmintan.mods.minefortress.core.services.ScreensLocator;
import net.remmintan.mods.minefortress.gui.FortressConfigurationScreen;
import net.remmintan.mods.minefortress.gui.FortressHandledScreensKt;
import net.remmintan.mods.minefortress.gui.building.functions.BuildingFunctionsRegistry;
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

        HandledScreens.register(FORTRESS_CRAFTING_SCREEN_HANDLER, FortressCraftingScreen::new);
        HandledScreens.register(FORTRESS_FURNACE_SCREEN_HANDLER, FortressFurnaceScreen::new);

        ScreensLocator.INSTANCE.register("fortress configuration", FortressConfigurationScreen::new);

        BlockRenderLayerMap.INSTANCE.putBlock(FortressBlocks.FORTRESS_CAMPFIRE, RenderLayer.getCutout());

        FortressHandledScreensKt.registerScreens();

        ClientNetworkReceivers.registerReceivers();
        FortressClientEvents.registerEvents();
        BuildingFunctionsRegistry.INSTANCE.register();
    }
}
