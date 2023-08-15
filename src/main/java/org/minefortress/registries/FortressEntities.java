package org.minefortress.registries;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.minefortress.entity.ArcherPawn;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.WarriorPawn;
import org.minefortress.entity.renderer.PawnRenderer;

import java.util.List;

public class FortressEntities {

    public static final float PAWN_WIDTH = 0.6f;
    public static final float PAWN_HEIGHT = 1.8f;
    public static final EntityType<Colonist> COLONIST_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("minefortress", "colonist"),
            FabricEntityTypeBuilder
                    .create(SpawnGroup.CREATURE, Colonist::new)
                    .dimensions(EntityDimensions.fixed(PAWN_WIDTH, PAWN_HEIGHT))
                    .build()
    );

    public static final EntityType<WarriorPawn> WARRIOR_PAWN_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("minefortress", "warrior_pawn"),
            FabricEntityTypeBuilder
                    .create(SpawnGroup.CREATURE, WarriorPawn::new)
                    .dimensions(EntityDimensions.fixed(PAWN_WIDTH, PAWN_HEIGHT))
                    .build()
    );

    public static final EntityType<ArcherPawn> ARCHER_PAWN_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("minefortress", "archer_pawn"),
            FabricEntityTypeBuilder
                    .create(SpawnGroup.CREATURE, ArcherPawn::new)
                    .dimensions(EntityDimensions.fixed(PAWN_WIDTH, PAWN_HEIGHT))
                    .build()
    );

    private static final List<EntityType<? extends Entity>> fortressAwareEntityTypes = List.of(
            COLONIST_ENTITY_TYPE,
            WARRIOR_PAWN_ENTITY_TYPE,
            ARCHER_PAWN_ENTITY_TYPE
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(FortressEntities.COLONIST_ENTITY_TYPE, Colonist.createAttributes());
        FabricDefaultAttributeRegistry.register(FortressEntities.WARRIOR_PAWN_ENTITY_TYPE, BasePawnEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(FortressEntities.ARCHER_PAWN_ENTITY_TYPE, ArcherPawn.createAttributes());
    }

    public static void registerRenderers() {
        EntityRendererRegistry.register(FortressEntities.COLONIST_ENTITY_TYPE, PawnRenderer::new);
        EntityRendererRegistry.register(FortressEntities.WARRIOR_PAWN_ENTITY_TYPE, PawnRenderer::new);
        EntityRendererRegistry.register(FortressEntities.ARCHER_PAWN_ENTITY_TYPE, PawnRenderer::new);
    }

    public static boolean isFortressAwareEntityType(EntityType<? extends Entity> entityType) {
        return fortressAwareEntityTypes.contains(entityType);
    }

}
