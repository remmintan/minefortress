package net.remmintan.mods.minefortress.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.blocks.building.FortressBuildingBlock;
import net.remmintan.mods.minefortress.blocks.building.FortressBuildingBlockEntity;
import net.remmintan.mods.minefortress.blocks.building.FortressBuildingBlockEntityRenderer;
import net.remmintan.mods.minefortress.blocks.campfire.FortressCampfireBlock;
import net.remmintan.mods.minefortress.blocks.campfire.FortressCampfireBlockEntity;
import net.remmintan.mods.minefortress.blocks.task.FortressTaskBlock;
import net.remmintan.mods.minefortress.blocks.task.FortressTaskBlockEntity;

public class FortressBlocks {

    public static Block SCAFFOLD_OAK_PLANKS;
    public static Block FORTRESS_BUILDING_CONFIGURATION;
    public static Block FORTRESS_BUILDING;
    public static Block FORTRESS_CAMPFIRE;
    public static Block FORTRESS_TASK;

    public static BlockEntityType<FortressScaffoldBlockEntity> SCAFFOLD_ENT_TYPE;
    public static BlockEntityType<FortressBuildingConfigurationBlockEntity> BUILDING_CONFIGURATION_ENT_TYPE;
    public static BlockEntityType<FortressBuildingBlockEntity> BUILDING_ENT_TYPE;
    public static BlockEntityType<FortressCampfireBlockEntity> CAMPFIRE_ENT_TYPE;
    public static BlockEntityType<FortressTaskBlockEntity> TASK_ENT_TYPE;

    public static void register() {
        final var scaffoldId = Identifier.of("minefortress", "scaffold_oak_planks");
        SCAFFOLD_OAK_PLANKS = Registry.register(Registries.BLOCK, scaffoldId, new FortressScaffoldBlock());
        FlammableBlockRegistry.getInstance(Blocks.FIRE).add(FortressBlocks.SCAFFOLD_OAK_PLANKS, 5, 20);
        SCAFFOLD_ENT_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                scaffoldId,
                FabricBlockEntityTypeBuilder.create(FortressScaffoldBlockEntity::new, SCAFFOLD_OAK_PLANKS).build()
        );


        final var buildingConfigurationBlockId = Identifier.of("minefortress", "building_configuration");
        FORTRESS_BUILDING_CONFIGURATION = Registry.register(Registries.BLOCK, buildingConfigurationBlockId, new FortressBuildingConfigurationBlock());
        BUILDING_CONFIGURATION_ENT_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                buildingConfigurationBlockId,
                FabricBlockEntityTypeBuilder.create(FortressBuildingConfigurationBlockEntity::new, FORTRESS_BUILDING_CONFIGURATION).build()
        );

        final var buildingBlockId = Identifier.of("minefortress", "building");
        FORTRESS_BUILDING = Registry.register(Registries.BLOCK, buildingBlockId, new FortressBuildingBlock());
        BUILDING_ENT_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                buildingBlockId,
                FabricBlockEntityTypeBuilder.create(FortressBuildingBlockEntity::new, FORTRESS_BUILDING).build()
        );
        BlockEntityRendererFactories.register(BUILDING_ENT_TYPE, ctx -> new FortressBuildingBlockEntityRenderer());

        final var campfireId = Identifier.of("minefortress", "fortress_campfire");
        FORTRESS_CAMPFIRE = Registry.register(Registries.BLOCK, campfireId, new FortressCampfireBlock());
        CAMPFIRE_ENT_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                campfireId,
                FabricBlockEntityTypeBuilder.create(FortressCampfireBlockEntity::new, FORTRESS_CAMPFIRE).build()
        );

        final var taskId = Identifier.of("minefortress", "fortress_task");
        FORTRESS_TASK = Registry.register(Registries.BLOCK, taskId, new FortressTaskBlock());
        TASK_ENT_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                taskId,
                FabricBlockEntityTypeBuilder.create(FortressTaskBlockEntity::new, FORTRESS_TASK).build()
        );
    }
}
