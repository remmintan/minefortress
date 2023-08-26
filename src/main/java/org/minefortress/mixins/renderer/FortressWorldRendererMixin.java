package org.minefortress.mixins.renderer;


import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.minefortress.fortress.FortressBorder;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.FortressState;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.MineFortressLabelsRenderer;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BiFunction;

@Mixin(WorldRenderer.class)
public abstract class FortressWorldRendererMixin  {

    private static final Identifier FORCEFIELD = new Identifier("textures/misc/forcefield.png");
    @Shadow @Final private MinecraftClient client;
    @Shadow private ClientWorld world;
    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    private MineFortressLabelsRenderer entityRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        final var fortressClient = (FortressMinecraftClient) client;
        this.entityRenderer = new MineFortressLabelsRenderer(
                client.textRenderer,
                fortressClient::get_SelectionManager,
                () -> fortressClient.get_FortressClientManager().getBuildingHealths()
        );
    }

    @Inject(method = "setupTerrain", at = @At("TAIL"))
    public void setupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        fortressClient.get_BlueprintRenderer().prepareForRender();
        fortressClient.get_CampfireRenderer().prepareForRender();
        fortressClient.get_SelectionRenderer().prepareForRender();
        fortressClient.get_TasksRenderer().prepareForRender();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal=2, target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    public void renderObjectsOnTerrain(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        final var cameraPos = camera.getPos();

        final var fortressClient = (FortressMinecraftClient) this.client;
        fortressClient.get_BlueprintRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z,  projectionMatrix);
        fortressClient.get_CampfireRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, projectionMatrix);
        fortressClient.get_SelectionRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, projectionMatrix);
        fortressClient.get_TasksRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, projectionMatrix);

        final var selectionManager = fortressClient.get_SelectionManager();
        final var immediate = this.bufferBuilders.getEntityVertexConsumers();
        final var vertexConsumer = immediate.getBuffer(RenderLayer.getLines());
        final FortressClientManager fcm = fortressClient.get_FortressClientManager();
        if (!selectionManager.isSelecting() && fcm.getState() == FortressState.BUILD) {
            if(ModUtils.isClientInFortressGamemode()) {
                final HitResult crosshairTarget = client.crosshairTarget;
                if(crosshairTarget instanceof BlockHitResult bhr) {
                    BlockPos pos = bhr.getBlockPos();
                    if(pos != null && !world.getBlockState(pos).isAir()) {
                        final List<BlockPos> buildingSelection = fcm.getBuildingSelection(pos);
                        for(BlockPos sel: buildingSelection) {
                            if(this.world.getWorldBorder().contains(sel)) {
                                final BlockState blockState = this.world.getBlockState(sel);
                                if(!blockState.isAir()) {
                                    this.drawBlockOutline(matrices, vertexConsumer, camera.getFocusedEntity(), cameraPos.x, cameraPos.y, cameraPos.z, sel, blockState);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal=11, target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    public void renderSelectionLabels(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        if(!client.options.hudHidden) {
            this.entityRenderer.prepare(camera);
            final var cameraPos = camera.getPos();
            final var immediate = this.bufferBuilders.getEntityVertexConsumers();
            this.entityRenderer.render(cameraPos.x, cameraPos.y, cameraPos.z, matrices, immediate, LightmapTextureManager.pack(15, 15));
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal=14, target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    public void renderTranslucent(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        renderTranslucent(matrices, camera, matrix4f);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal=18, target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", shift = At.Shift.BY, by = -3))
    public void renderTranslucentBuffer(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        renderTranslucent(matrices, camera, matrix4f);
    }

    @Unique
    private WorldBorder getWorldBorder(ClientWorld instance) {
        final var selectingBlueprint = ModUtils.getBlueprintManager().isSelecting();
        final var selecting = ModUtils.getSelectionManager().isSelecting();
        final var selectingArea = ModUtils.getAreasClientManager().isSelecting();
        final var centerNotSet = ModUtils.getFortressClientManager().isCenterNotSet();
        final var influenceManager = ModUtils.getInfluenceManager();
        final var isCapturingPoint = influenceManager.isSelecting();

        if(selectingBlueprint || selecting || selectingArea || centerNotSet || isCapturingPoint)
            return influenceManager
                    .getFortressBorder()
                    .orElseGet(instance::getWorldBorder);
        else
            return instance.getWorldBorder();
    }

    @Inject(method="renderWorldBorder", at=@At("HEAD"), cancellable = true)
    public void renderCustomWorldBorder(Camera camera, CallbackInfo ci) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        WorldBorder worldBorder = getWorldBorder(this.world);
        double viewDistance = this.client.options.getClampedViewDistance() * 16;
        if (!(camera.getPos().x < worldBorder.getBoundEast() - viewDistance) || !(camera.getPos().x > worldBorder.getBoundWest() + viewDistance) || !(camera.getPos().z < worldBorder.getBoundSouth() - viewDistance) || !(camera.getPos().z > worldBorder.getBoundNorth() + viewDistance)) {
            double e = 1.0 - worldBorder.getDistanceInsideBorder(camera.getPos().x, camera.getPos().z) / viewDistance;
            e = Math.pow(e, 4.0);
            e = MathHelper.clamp(e, 0.0, 1.0);

            double cameraDistance = this.client.gameRenderer.getFarPlaneDistance();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
            RenderSystem.setShaderTexture(0, FORCEFIELD);
            RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
            MatrixStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.push();
            RenderSystem.applyModelViewMatrix();
            int i = worldBorder.getStage().getColor();
            float j = (float)(i >> 16 & 255) / 255.0F;
            float k = (float)(i >> 8 & 255) / 255.0F;
            float l = (float)(i & 255) / 255.0F;
            RenderSystem.setShaderColor(j, k, l, (float)e);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.polygonOffset(-3.0F, -3.0F);
            RenderSystem.enablePolygonOffset();
            RenderSystem.disableCull();


            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            BiFunction<Double, Double, Boolean> shouldRenderBoundFunc = (x, z) -> worldBorder instanceof FortressBorder fb && fb.shouldRenderBound(x, z);

            renderParticularWorldBorder(bufferBuilder, worldBorder, viewDistance, camera, cameraDistance, shouldRenderBoundFunc);
            if(worldBorder instanceof FortressBorder fortressBorder) {
                fortressBorder
                        .getAdditionalBorders()
                        .forEach(border -> renderParticularWorldBorder(bufferBuilder, border, viewDistance, camera, cameraDistance, shouldRenderBoundFunc));
            }


            BufferRenderer.draw(bufferBuilder.end());
            RenderSystem.enableCull();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.disableBlend();
            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
        }
        ci.cancel();
    }

    private static void renderParticularWorldBorder(BufferBuilder bufferBuilder,
                                                    WorldBorder worldBorder,
                                                    double viewDistance,
                                                    Camera camera,
                                                    double cameraDistance,
                                                    BiFunction<Double, Double, Boolean> shouldRenderBound) {
        float m = (float)(Util.getMeasuringTimeMs() % 3000L) / 3000.0F;
        double cameraX = camera.getPos().x;
        double cameraZ = camera.getPos().z;
        final var boundNorth = worldBorder.getBoundNorth();
        double q = Math.max(MathHelper.floor(cameraZ - viewDistance), boundNorth);
        final var boundSouth = worldBorder.getBoundSouth();
        double r = Math.min(MathHelper.ceil(cameraZ + viewDistance), boundSouth);
        float p = (float)(cameraDistance - MathHelper.fractionalPart(camera.getPos().y));
        float v;
        float s;
        double t;
        double u;

        final var centerX = worldBorder.getCenterX();
        final var centerZ = worldBorder.getCenterZ();

        final var boundEast = worldBorder.getBoundEast();
        if (cameraX > boundEast - viewDistance && shouldRenderBound.apply(boundEast, centerZ)) {
            s = 0.0F;

            for(t = q; t < r; s += 0.5F) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5F;
                bufferBuilder.vertex(boundEast - cameraX, -cameraDistance, t - cameraZ).texture(m - s, m + p).next();
                bufferBuilder.vertex(boundEast - cameraX, -cameraDistance, t + u - cameraZ).texture(m - (v + s), m + p).next();
                bufferBuilder.vertex(boundEast - cameraX, cameraDistance, t + u - cameraZ).texture(m - (v + s), m + 0.0F).next();
                bufferBuilder.vertex(boundEast - cameraX, cameraDistance, t - cameraZ).texture(m - s, m + 0.0F).next();
                ++t;
            }
        }

        final var boundWest = worldBorder.getBoundWest();
        if (cameraX < boundWest + viewDistance && shouldRenderBound.apply(boundWest, centerZ)) {
            s = 0.0F;

            for(t = q; t < r; s += 0.5F) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5F;
                bufferBuilder.vertex(boundWest - cameraX, -cameraDistance, t - cameraZ).texture(m + s, m + p).next();
                bufferBuilder.vertex(boundWest - cameraX, -cameraDistance, t + u - cameraZ).texture(m + v + s, m + p).next();
                bufferBuilder.vertex(boundWest - cameraX, cameraDistance, t + u - cameraZ).texture(m + v + s, m + 0.0F).next();
                bufferBuilder.vertex(boundWest - cameraX, cameraDistance, t - cameraZ).texture(m + s, m + 0.0F).next();
                ++t;
            }
        }

        q = Math.max(MathHelper.floor(cameraX - viewDistance), boundWest);
        r = Math.min(MathHelper.ceil(cameraX + viewDistance), boundEast);
        if (cameraZ > boundSouth - viewDistance && shouldRenderBound.apply(centerX, boundSouth)) {
            s = 0.0F;

            for(t = q; t < r; s += 0.5F) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5F;
                bufferBuilder.vertex(t - cameraX, -cameraDistance, boundSouth - cameraZ).texture(m + s, m + p).next();
                bufferBuilder.vertex(t + u - cameraX, -cameraDistance, boundSouth - cameraZ).texture(m + v + s, m + p).next();
                bufferBuilder.vertex(t + u - cameraX, cameraDistance, boundSouth - cameraZ).texture(m + v + s, m + 0.0F).next();
                bufferBuilder.vertex(t - cameraX, cameraDistance, boundSouth - cameraZ).texture(m + s, m + 0.0F).next();
                ++t;
            }
        }

        if (cameraZ < boundNorth + viewDistance && shouldRenderBound.apply(centerX, boundNorth)) {
            s = 0.0F;

            for(t = q; t < r; s += 0.5F) {
                u = Math.min(1.0, r - t);
                v = (float)u * 0.5F;
                bufferBuilder.vertex(t - cameraX, -cameraDistance, boundNorth - cameraZ).texture(m - s, m + p).next();
                bufferBuilder.vertex(t + u - cameraX, -cameraDistance, boundNorth - cameraZ).texture(m - (v + s), m + p).next();
                bufferBuilder.vertex(t + u - cameraX, cameraDistance, boundNorth - cameraZ).texture(m - (v + s), m + 0.0F).next();
                bufferBuilder.vertex(t - cameraX, cameraDistance, boundNorth - cameraZ).texture(m - s, m + 0.0F).next();
                ++t;
            }
        }
    }

    @Unique
    private void renderTranslucent(MatrixStack matrices, Camera camera, Matrix4f matrix4f) {
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        fortressClient.get_SelectionRenderer().renderTranslucent(matrices, camera.getPos().x, camera.getPos().y, camera.getPos().z, matrix4f);
        fortressClient.get_BlueprintRenderer().renderTranslucent(matrices, camera.getPos().x, camera.getPos().y, camera.getPos().z, matrix4f);
    }

    @Unique
    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState) {
        final Vector4f clickColors = ((FortressMinecraftClient) client).get_SelectionManager().getClickColor();
        final var outlineShape = blockState.getOutlineShape(this.world, blockPos, ShapeContext.of(entity));
        WorldRenderer.drawShapeOutline(
                matrices,
                vertexConsumer,
                outlineShape,
                (double)blockPos.getX() - d,
                (double)blockPos.getY() - e,
                (double)blockPos.getZ() - f,
                clickColors.x(),
                clickColors.y(),
                clickColors.z(),
                clickColors.w(),
                true
        );
    }

}
