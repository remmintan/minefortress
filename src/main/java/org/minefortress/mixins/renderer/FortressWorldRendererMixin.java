package org.minefortress.mixins.renderer;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.border.WorldBorder;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.FortressState;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.MineFortressEntityRenderer;
import org.minefortress.selections.SelectionManager;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "renderWorldBorder", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    public WorldBorder getWorldBorder(ClientWorld instance) {
        final var selectingBlueprint = ModUtils.getBlueprintManager().hasSelectedBlueprint();
        final var selecting = ModUtils.getSelectionManager().isSelecting();
        final var selectingArea = ModUtils.getAreasClientManager().isSelecting();

        if(selectingBlueprint || selecting || selectingArea)
            return ModUtils.getFortressClientManager()
                    .getFortressBorder()
                    .orElseGet(instance::getWorldBorder);
        else
            return instance.getWorldBorder();
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
