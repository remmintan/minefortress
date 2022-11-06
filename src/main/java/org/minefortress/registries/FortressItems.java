package org.minefortress.registries;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class FortressItems {

    public static final Item COLONIST_SPAWN_EGG = new SpawnEggItem(
            FortressEntities.COLONIST_ENTITY_TYPE,
            0x563c33,
            0xeaa430,
            new Item.Settings().group(ItemGroup.MISC)
    );

    public static final Item WARRIOR_PAWN_SPAWN_EGG = new SpawnEggItem(
            FortressEntities.WARRIOR_PAWN_ENTITY_TYPE,
            0x563c33,
            0x7f7f7f,
            new Item.Settings().group(ItemGroup.MISC)
    );

    public static final Item SCAFFOLD_OAK_PLANKS = new BlockItem(
            FortressBlocks.SCAFFOLD_OAK_PLANKS,
            new Item.Settings().rarity(Rarity.EPIC)
    );

    public static void register() {
        Registry.register(Registry.ITEM, new Identifier("minefortress", "scaffold_oak_planks_b_1_1_0"), FortressItems.SCAFFOLD_OAK_PLANKS);
        Registry.register(Registry.ITEM, new Identifier("minefortress", "colonist_spawn_egg"), FortressItems.COLONIST_SPAWN_EGG);
        Registry.register(Registry.ITEM, new Identifier("minefortress", "warrior_pawn_spawn_egg"), FortressItems.WARRIOR_PAWN_SPAWN_EGG);
    }

}
