package org.minefortress.mixins.renderer;

import com.chocohead.mm.api.ClassTinkerers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.GameMode;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.FortressRenderLayer;
import org.minefortress.renderer.MineFortressEntityRenderer;
import org.minefortress.selections.ClickType;
import org.minefortress.selections.SelectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldRenderer.class)
public abstract class FortressWorldRendererMixin  {

    @Shadow @Final private MinecraftClient client;
    @Shadow private ClientWorld world;
    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Shadow private static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {}
    private MineFortressEntityRenderer entityRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftClient client, BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        this.entityRenderer = new MineFortressEntityRenderer(client.textRenderer, ((FortressMinecraftClient) client)::getSelectionManager);
    }

    @Inject(method = "setWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;setWorld(Lnet/minecraft/world/World;)V"))
    public void setWorld(ClientWorld world, CallbackInfo ci) {
        this.entityRenderer.setLevel(world);
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
            this.entityRenderer.prepare(this.world, camera);
            this.entityRenderer.render(cameraPos.x, cameraPos.y, cameraPos.z, matrices, immediate, LightmapTextureManager.pack(15, 15));
        }

        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        fortressClient.getBlueprintRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z,  matrix4f);
        fortressClient.getCampfireRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, matrix4f);
        fortressClient.getSelectionRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, matrix4f);
        fortressClient.getTasksRenderer().render(matrices, cameraPos.x, cameraPos.y, cameraPos.z, matrix4f);

        SelectionManager selectionManager = fortressClient.getSelectionManager();
        VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getLines());
        if (!selectionManager.isSelecting()) {
            final GameMode currentGameMode = this.client.interactionManager.getCurrentGameMode();
            if(currentGameMode == ClassTinkerers.getEnum(GameMode.class, "FORTRESS")) {
                final FortressClientManager fortressClientManager = fortressClient.getFortressClientManager();
                final HitResult crosshairTarget = client.crosshairTarget;
                if(crosshairTarget instanceof BlockHitResult bhr) {
                    BlockPos pos = bhr.getBlockPos();
                    if(pos != null && !world.getBlockState(pos).isAir() && !fortressClientManager.isInCombat()) {
                        final List<BlockPos> buildingSelection = fortressClientManager.getBuildingSelection(pos);
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
