package org.minefortress;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.blocks.FortressBlocks;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.services.FortressServiceLocator;
import net.remmintan.mods.minefortress.gui.FortressHandledScreensKt;
import net.remmintan.mods.minefortress.networking.registries.ServerNetworkReceivers;
import org.minefortress.commands.CommandsManager;
import org.minefortress.fortress.ServerFortressManager;
import org.minefortress.fortress.ServerManagersProvider;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandler;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreenHandler;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressItems;
import org.minefortress.registries.events.FortressServerEvents;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MineFortressMod implements ModInitializer {

    public static final String BLUEPRINTS_FOLDER_NAME = "minefortress-blueprints";
    public static final String BLUEPRINTS_EXTENSION = ".zip";
    public static final String MOD_ID = "minefortress";

    private static final Identifier FORTRESS_CRAFTING_SCREEN_HANDLER_ID = new Identifier(MOD_ID, "fortress_crafting_handler");
    private static final Identifier FORTRESS_FURNACE_SCREEN_HANDLER_ID = new Identifier(MOD_ID, "fortress_furnace_handler");
    public static final ScreenHandlerType<FortressCraftingScreenHandler> FORTRESS_CRAFTING_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(FORTRESS_CRAFTING_SCREEN_HANDLER_ID, FortressCraftingScreenHandler::new);
    public static final ScreenHandlerType<FortressFurnaceScreenHandler> FORTRESS_FURNACE_SCREEN_HANDLER =  ScreenHandlerRegistry.registerSimple(FORTRESS_FURNACE_SCREEN_HANDLER_ID, FortressFurnaceScreenHandler::new);

    private static final ExecutorService executor;

    static  {
        var incrementer = new AtomicInteger(0);
        executor = Executors.newCachedThreadPool(r ->
                new Thread(r, "MineFortress Worker " + incrementer.incrementAndGet()));
    }

    @Override
    public void onInitialize() {
        FortressBlocks.register();
        FortressEntities.register();
        FortressItems.register();
        FortressServerEvents.register();
        NetworkReaders.register();

        CommandsManager.registerCommands();
        ServerNetworkReceivers.registerReceivers();
        FortressHandledScreensKt.registerHandlerTypes();

        FortressServiceLocator.INSTANCE.register(IServerManagersProvider.class, ServerManagersProvider::new);
        FortressServiceLocator.INSTANCE.register(IServerFortressManager.class, ServerFortressManager::new);
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

}
