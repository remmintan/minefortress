package org.minefortress;


import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.minefortress.commands.CommandsManager;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandler;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreenHandler;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.registries.*;

public class MineFortressMod implements ModInitializer {

    public static final GameMode FORTRESS = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");
    public static final String BLUEPRINTS_FOLDER_NAME = "minefortress-blueprints";
    public static final String BLUEPRINTS_EXTENSION = ".blueprints";
    public static final String MOD_ID = "minefortress";

    private static final Identifier FORTRESS_CRAFTING_SCREEN_HANDLER_ID = new Identifier(MOD_ID, "fortress_crafting_handler");
    private static final Identifier FORTRESS_FURNACE_SCREEN_HANDLER_ID = new Identifier(MOD_ID, "fortress_furnace_handler");
    public static final ScreenHandlerType<FortressCraftingScreenHandler> FORTRESS_CRAFTING_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(FORTRESS_CRAFTING_SCREEN_HANDLER_ID, FortressCraftingScreenHandler::new);
    public static final ScreenHandlerType<FortressFurnaceScreenHandler> FORTRESS_FURNACE_SCREEN_HANDLER =  ScreenHandlerRegistry.registerSimple(FORTRESS_FURNACE_SCREEN_HANDLER_ID, FortressFurnaceScreenHandler::new);

    @Override
    public void onInitialize() {
        FortressKeybindings.init();
        FortressBlocks.register();
        FortressEntities.register();
        FortressItems.register();
        FortressServerEvents.register();

        CommandsManager.registerCommands();
        FortressServerNetworkHelper.registerReceivers();
    }

}
