package net.remmintan.mods.minefortress.building;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FortressBlocks {

    public static final Block SCAFFOLD_OAK_PLANKS = new FortressScaffoldBlock();

    public static BlockEntityType<FortressScaffoldBlockEntity> SCAFFOLD_ENT_TYPE;

    public static void register() {
        final var id = Identifier.of("minefortress", "scaffold_oak_planks");
        Registry.register(Registries.BLOCK, id, FortressBlocks.SCAFFOLD_OAK_PLANKS);
        FlammableBlockRegistry.getInstance(Blocks.FIRE).add(FortressBlocks.SCAFFOLD_OAK_PLANKS, 5, 20);

        SCAFFOLD_ENT_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                id,
                FabricBlockEntityTypeBuilder.create(FortressScaffoldBlockEntity::new, SCAFFOLD_OAK_PLANKS).build()
        );
    }
}
