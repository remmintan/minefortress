package org.minefortress.entity.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.fisher.FortressFishingBobberEntity;

@Environment(EnvType.CLIENT)
public class FortressFishingBobberEntityRenderer extends EntityRenderer<FortressFishingBobberEntity> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/fishing_hook.png");
    private static final RenderLayer LAYER;
    private static final double field_33632 = 960.0;

    public FortressFishingBobberEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    public void render(FortressFishingBobberEntity fishingBobberEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Colonist pawnOwner = fishingBobberEntity.getPawnOwner();
        if (pawnOwner != null) {
            matrixStack.push();
            matrixStack.push();
            matrixStack.scale(0.5F, 0.5F, 0.5F);
            matrixStack.multiply(this.dispatcher.getRotation());
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            MatrixStack.Entry entry = matrixStack.peek();
            Matrix4f matrix4f = entry.getPositionMatrix();
            Matrix3f matrix3f = entry.getNormalMatrix();
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
            vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 0, 0, 1);
            vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 0, 1, 1);
            vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 1, 1, 0);
            vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 1, 0, 0);
            matrixStack.pop();
            int j = pawnOwner.getMainArm() == Arm.RIGHT ? 1 : -1;
            ItemStack itemStack = pawnOwner.getMainHandStack();
            if (!itemStack.isOf(Items.FISHING_ROD)) {
                j = -j;
            }

            float h = pawnOwner.getHandSwingProgress(g);
            float k = MathHelper.sin(MathHelper.sqrt(h) * 3.1415927F);
            float l = MathHelper.lerp(g, pawnOwner.prevBodyYaw, pawnOwner.bodyYaw) * 0.017453292F;
            double d = MathHelper.sin(l);
            double e = MathHelper.cos(l);
            double m = (double)j * 0.35;
            double n = 0.8;
            double o;
            double p;
            double q;
            float r;
            double s;

            o = MathHelper.lerp(g, pawnOwner.prevX, pawnOwner.getX()) - e * m - d * 0.8;
            p = pawnOwner.prevY + (double)pawnOwner.getStandingEyeHeight() + (pawnOwner.getY() - pawnOwner.prevY) * (double)g - 0.45;
            q = MathHelper.lerp(g, pawnOwner.prevZ, pawnOwner.getZ()) - d * m + e * 0.8;
            r = pawnOwner.isInSneakingPose() ? -0.1875F : 0.0F;


            s = MathHelper.lerp(g, fishingBobberEntity.prevX, fishingBobberEntity.getX());
            double t = MathHelper.lerp(g, fishingBobberEntity.prevY, fishingBobberEntity.getY()) + 0.25;
            double u = MathHelper.lerp(g, fishingBobberEntity.prevZ, fishingBobberEntity.getZ());
            float v = (float)(o - s);
            float w = (float)(p - t) + r;
            float x = (float)(q - u);
            VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getLineStrip());
            MatrixStack.Entry entry2 = matrixStack.peek();

            for(int z = 0; z <= 16; ++z) {
                renderFishingLine(v, w, x, vertexConsumer2, entry2, percentage(z, 16), percentage(z + 1, 16));
            }

            matrixStack.pop();
            super.render(fishingBobberEntity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }

    private static float percentage(int value, int max) {
        return (float)value / (float)max;
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {
        buffer.vertex(matrix, x - 0.5F, (float)y - 0.5F, 0.0F).color(255, 255, 255, 255).texture((float)u, (float)v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
    }

    private static void renderFishingLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {
        float f = x * segmentStart;
        float g = y * (segmentStart * segmentStart + segmentStart) * 0.5F + 0.25F;
        float h = z * segmentStart;
        float i = x * segmentEnd - f;
        float j = y * (segmentEnd * segmentEnd + segmentEnd) * 0.5F + 0.25F - g;
        float k = z * segmentEnd - h;
        float l = MathHelper.sqrt(i * i + j * j + k * k);
        i /= l;
        j /= l;
        k /= l;
        buffer.vertex(matrices.getPositionMatrix(), f, g, h).color(0, 0, 0, 255).normal(matrices.getNormalMatrix(), i, j, k).next();
    }

    public Identifier getTexture(FortressFishingBobberEntity fishingBobberEntity) {
        return TEXTURE;
    }

    static {
        LAYER = RenderLayer.getEntityCutout(TEXTURE);
    }
}

