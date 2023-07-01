package org.minefortress.mixins.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.border.WorldBorder;
import org.minefortress.fortress.FortressBorder;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.FortressState;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.MineFortressLabelsRenderer;
import org.minefortress.selections.SelectionManager;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow private static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {}

    private MineFortressLabelsRenderer entityRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftClient client, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        final var fortressClient = (FortressMinecraftClient) client;
        this.entityRenderer = new MineFortressLabelsRenderer(
                client.textRenderer,
                fortressClient::getSelectionManager,
                () -> fortressClient.getFortressClientManager().getBuildingHealths()
        );
    }

    @Inject(method = "setupTerrain", at = @At("TAIL"))
    public void setupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        fortressClient.getBlueprintRenderer().prepareForRender();
        fortressClient.getCampfireRenderer().prepareForRender();
        fortressClient.getSelectionRenderer().prepareForRender();
        fortressClient.getTasksRenderer().prepareForRender();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal=2, target = "Lnet/minecraft/client/render/WorldRenderer;checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V", shift = At.Shift.AFTER))
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        final Vec3d cameraPos = camera.getPos();
        final VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();

        if(!client.options.hudHidden) {
            this.entityRenderer.prepare(camera);
            this.entityRenderer.render(cameraPos.x, cameraPos.y, cameraPos.z, matrices, immediate, LightmapTextureManager.pack(15, 15));
        }

        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        fortressClient.getBlueprintRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z,  matrix4f);
        fortressClient.getCampfireRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, matrix4f);
        fortressClient.getSelectionRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, matrix4f);
        fortressClient.getTasksRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, matrix4f);

        SelectionManager selectionManager = fortressClient.getSelectionManager();
        VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getLines());
        final FortressClientManager fcm = fortressClient.getFortressClientManager();
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

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal=14, target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    public void renderTranslucent(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        renderTranslucent(matrices, camera, matrix4f);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal=18, target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", shift = At.Shift.BY, by = -3))
    public void renderTranslucentBuffer(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        renderTranslucent(matrices, camera, matrix4f);
    }

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
        double viewDistance = this.client.options.getViewDistance() * 16;
        if (!(camera.getPos().x < worldBorder.getBoundEast() - viewDistance) || !(camera.getPos().x > worldBorder.getBoundWest() + viewDistance) || !(camera.getPos().z < worldBorder.getBoundSouth() - viewDistance) || !(camera.getPos().z > worldBorder.getBoundNorth() + viewDistance)) {
            double e = 1.0 - worldBorder.getDistanceInsideBorder(camera.getPos().x, camera.getPos().z) / viewDistance;
            e = Math.pow(e, 4.0);
            e = MathHelper.clamp(e, 0.0, 1.0);

            double cameraDistance = this.client.gameRenderer.method_32796();
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
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
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

            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
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

    private void renderTranslucent(MatrixStack matrices, Camera camera, Matrix4f matrix4f) {
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        fortressClient.getSelectionRenderer().renderTranslucent(matrices, camera.getPos().x, camera.getPos().y, camera.getPos().z, matrix4f);
        fortressClient.getBlueprintRenderer().renderTranslucent(matrices, camera.getPos().x, camera.getPos().y, camera.getPos().z, matrix4f);
    }

    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState) {
        final Vector4f clickColors = ((FortressMinecraftClient) client).getSelectionManager().getClickColor();
        drawShapeOutline(matrices, vertexConsumer, blockState.getOutlineShape(this.world, blockPos, ShapeContext.of(entity)), (double)blockPos.getX() - d, (double)blockPos.getY() - e, (double)blockPos.getZ() - f, clickColors.getX(), clickColors.getY(), clickColors.getZ(), clickColors.getW());
    }

}
