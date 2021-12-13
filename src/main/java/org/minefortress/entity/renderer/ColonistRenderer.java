package org.minefortress.entity.renderer;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import org.minefortress.entity.Colonist;

public class ColonistRenderer extends BipedEntityRenderer<Colonist, BipedEntityModel<Colonist>> {

    private static final Identifier TEXTURE = new Identifier("textures/entity/steve.png");

    public ColonistRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
        BipedEntityModel<Colonist> innerArmor = new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR));
        BipedEntityModel<Colonist> outerArmor = new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR));

        this.addFeature(new ArmorFeatureRenderer<>(this, innerArmor, outerArmor));
    }

    @Override
    public Identifier getTexture(Colonist p_114482_) {
        return TEXTURE;
    }

    @Override
    protected boolean hasLabel(Colonist p_115333_) {
        return false;
    }
}