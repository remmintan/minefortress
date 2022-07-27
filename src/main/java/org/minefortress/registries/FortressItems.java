package org.minefortress.registries;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;

public class FortressItems {

    public static final Item COLONIST_SPAWN_EGG = new SpawnEggItem(
            FortressEntities.COLONIST_ENTITY_TYPE,
            0x563c33,
            0xeaa430,
            new Item.Settings().group(ItemGroup.MISC)
    );

    public static final Item SCAFFOLD_OAK_PLANKS = new BlockItem(
            FortressBlocks.SCAFFOLD_OAK_PLANKS,
            new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)
    );

}
