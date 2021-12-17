package org.minefortress.registries;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;

public class FortressBlocks {

    public static final Block SCAFFOLD_OAK_PLANKS = new Block(
            FabricBlockSettings
                    .of(Material.WOOD)
                    .strength(2.0f, 3.0f)
                    .sounds(BlockSoundGroup.WOOD)
    );

}
