package org.minefortress;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.minefortress.entity.Colonist;

public class FortressEntities {

    public static final EntityType<Colonist> COLONIST_ENTITY_TYPE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("minefortress", "colonist"),
            FabricEntityTypeBuilder
                    .create(SpawnGroup.CREATURE, Colonist::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .build()
    );

    public static final EntityModelLayer COLONIST_MODEL_LAYER = new EntityModelLayer(
            new Identifier("minefortress", "colonist"), "main");

}
