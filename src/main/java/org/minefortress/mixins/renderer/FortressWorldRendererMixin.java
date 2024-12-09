package org.minefortress.mixins.renderer;


import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.renderer.MineFortressLabelsRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldRenderer.class)
public abstract class FortressWorldRendererMixin  {

    @Shadow @Final private MinecraftClient client;
    @Shadow private ClientWorld world;
    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Unique
    private MineFortressLabelsRenderer entityRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftClient client, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        final var fortressClient = (IClientManagersProvider) client;
        this.entityRenderer = new MineFortressLabelsRenderer(
                client.textRenderer,
                fortressClient::get_SelectionManager,
                () -> CoreModUtils.getBuildingsManager().getBuildingHealths()
        );
    }

    @Inject(method = "setupTerrain", at = @At("TAIL"))
    public void setupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        final IFortressMinecraftClient fortressClient = (IFortressMinecraftClient) this.client;
        fortressClient.get_BlueprintRenderer().prepareForRender();
        fortressClient.get_SelectionRenderer().prepareForRender();
        fortressClient.get_TasksRenderer().prepareForRender();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal=2, target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDDLorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    public void renderObjectsOnTerrain(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        final var cameraPos = camera.getPos();

        final var fortressClient = (IFortressMinecraftClient) this.client;
        fortressClient.get_BlueprintRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z,  projectionMatrix);
        fortressClient.get_SelectionRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, projectionMatrix);
        fortressClient.get_TasksRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, projectionMatrix);

        if (!FortressGamemodeUtilsKt.isClientInFortressGamemode()) return;

        final var selectionManager = CoreModUtils.getSelectionManager();
        final var immediate = this.bufferBuilders.getEntityVertexConsumers();
        final var vertexConsumer = immediate.getBuffer(RenderLayer.getLines());
        final var fcm = CoreModUtils.getFortressClientManager();
        if (!selectionManager.isSelecting() && (fcm.getState() == FortressState.BUILD_EDITING || fcm.getState() == FortressState.BUILD_SELECTION)){
            final var target = client.crosshairTarget;
            if(target instanceof BlockHitResult bhr) {
                final var pos = bhr.getBlockPos();
                if(pos != null && !world.getBlockState(pos).isAir()) {
                    final var buildingSelection = CoreModUtils.getBuildingsManager().getBuildingSelection(pos);
                    renderBuildingSelection(matrices, camera, buildingSelection, vertexConsumer, cameraPos);
                }
            }
        }
    }

    @Unique
    private void renderBuildingSelection(MatrixStack matrices, Camera camera, List<BlockPos> buildingSelection, VertexConsumer vertexConsumer, Vec3d cameraPos) {
        for(BlockPos sel: buildingSelection) {
            final BlockState blockState = this.world.getBlockState(sel);
            if(!blockState.isAir()) {
                this.drawBlockOutline(matrices, vertexConsumer, camera.getFocusedEntity(), cameraPos.x, cameraPos.y, cameraPos.z, sel, blockState);
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
    private void renderTranslucent(MatrixStack matrices, Camera camera, Matrix4f matrix4f) {
        final IFortressMinecraftClient fortressClient = (IFortressMinecraftClient) this.client;
        fortressClient.get_SelectionRenderer().renderTranslucent(matrices, camera.getPos().x, camera.getPos().y, camera.getPos().z, matrix4f);
        fortressClient.get_BlueprintRenderer().renderTranslucent(matrices, camera.getPos().x, camera.getPos().y, camera.getPos().z, matrix4f);
    }

    @Unique
    private void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState) {
        final Vector4f clickColors = ((IClientManagersProvider) client).get_SelectionManager().getClickColor();
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
