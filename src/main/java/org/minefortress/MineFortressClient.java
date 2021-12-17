package org.minefortress;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.minefortress.entity.renderer.ColonistRenderer;
import org.minefortress.registries.FortressEntities;

public class MineFortressClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(FortressEntities.COLONIST_ENTITY_TYPE, ColonistRenderer::new);
//        EntityModelLayerRegistry.registerModelLayer(FortressEntities.COLONIST_MODEL_LAYER, BipedEntityModel<Colonist>::getModelData);
    }
}
