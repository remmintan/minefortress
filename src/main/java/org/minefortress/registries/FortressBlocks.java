package org.minefortress.registries;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class FortressBlocks {

    public static final Block SCAFFOLD_OAK_PLANKS = new Block(
            FabricBlockSettings
                    .create()
                    .strength(2.0f, 3.0f)
                    .sounds(BlockSoundGroup.WOOD)
    );

    public static void register() {
        Registry.register(Registries.BLOCK, new Identifier("minefortress", "scaffold_oak_planks"), FortressBlocks.SCAFFOLD_OAK_PLANKS);
        FlammableBlockRegistry.getInstance(Blocks.FIRE).add(FortressBlocks.SCAFFOLD_OAK_PLANKS, 5, 20);
    }
}
