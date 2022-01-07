package org.minefortress;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.minefortress.entity.Colonist;
import org.minefortress.network.ServerboundCancelTaskPacket;
import org.minefortress.network.ServerboundSimpleSelectionTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.registries.FortressBlocks;
import org.minefortress.registries.FortressEntities;
import org.minefortress.registries.FortressItems;

public class MineFortressMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // blocks
        Registry.register(Registry.BLOCK, new Identifier("minefortress", "scaffold_oak_planks"), FortressBlocks.SCAFFOLD_OAK_PLANKS);
        FlammableBlockRegistry.getInstance(Blocks.FIRE).add(FortressBlocks.SCAFFOLD_OAK_PLANKS, 5, 20);

        // entities
        FabricDefaultAttributeRegistry.register(FortressEntities.COLONIST_ENTITY_TYPE, Colonist.createAttributes());
        Registry.register(Registry.ITEM, new Identifier("minefortress", "colonist_spawn_egg"), FortressItems.COLONIST_SPAWN_EGG);

        // networking
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.NEW_TASK, ServerboundSimpleSelectionTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.CANCEL_TASK, ServerboundCancelTaskPacket::new);
    }
}
