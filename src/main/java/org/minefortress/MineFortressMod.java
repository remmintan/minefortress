package org.minefortress;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.resources.craft.FortressCraftingScreenHandler;
import org.minefortress.network.*;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.registries.FortressBlocks;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressItems;

public class MineFortressMod implements ModInitializer {

    public static final String MOD_ID = "minefortress";

    private static final Identifier FORTRESS_CRAFTING_SCREEN_HANDLER_ID = new Identifier(MOD_ID, "fortress_crafting_handler");
    public static final ScreenHandlerType<FortressCraftingScreenHandler> FORTRESS_CRAFTING_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(FORTRESS_CRAFTING_SCREEN_HANDLER_ID, FortressCraftingScreenHandler::new);

    @Override
    public void onInitialize() {
        // blocks
        Registry.register(Registry.BLOCK, new Identifier("minefortress", "scaffold_oak_planks"), FortressBlocks.SCAFFOLD_OAK_PLANKS);
        FlammableBlockRegistry.getInstance(Blocks.FIRE).add(FortressBlocks.SCAFFOLD_OAK_PLANKS, 5, 20);

        // entities
        FabricDefaultAttributeRegistry.register(FortressEntities.COLONIST_ENTITY_TYPE, Colonist.createAttributes());
        Registry.register(Registry.ITEM, new Identifier("minefortress", "colonist_spawn_egg"), FortressItems.COLONIST_SPAWN_EGG);

        // networking
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.NEW_SELECTION_TASK, ServerboundSimpleSelectionTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.NEW_BLUEPRINT_TASK, ServerboundBlueprintTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.CANCEL_TASK, ServerboundCancelTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SET_CENTER, ServerboundFortressCenterSetPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, ServerboundEditBlueprintPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SAVE_EDIT_BLUEPRINT, ServerboundFinishEditBlueprintPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_CUT_TREES_TASK, ServerboundCutTreesTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_ROADS_TASK, ServerboundRoadsTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_STATE_CHANGE, ServerboundChangeProfessionStatePacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SET_GAMEMODE, ServerboundSetGamemodePacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SET_TICKS_SPEED, ServerboundSetTickSpeedPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE, ServerboundOpenCraftingScreenPacket::new);
    }
}
