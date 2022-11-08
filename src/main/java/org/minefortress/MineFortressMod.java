package org.minefortress;


import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.minefortress.commands.CommandsManager;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandler;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreenHandler;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.registries.FortressBlocks;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressItems;
import org.minefortress.utils.ModUtils;

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
        FortressBlocks.register();
        FortressEntities.register();
        FortressItems.register();

        CommandsManager.registerCommands();
        registerEvents();
        FortressServerNetworkHelper.registerReceivers();
    }

    public static void registerEvents() {
        EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> {
            if(ModUtils.isFortressGamemode(entity)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, pos, dir) -> {
            if(ModUtils.isFortressGamemode(entity)) {
                final var rotationVector = entity.getRotationVector();
                return Direction.getFacing(rotationVector.x, rotationVector.y, rotationVector.z);
            }
            return dir;
        });

        EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register((player, pos, vanilla) -> {
            if(ModUtils.isFortressGamemode(player)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

}
