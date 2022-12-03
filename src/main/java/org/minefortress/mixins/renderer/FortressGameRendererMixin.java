package org.minefortress.mixins.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressGameRenderer;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.CameraTools;
import org.minefortress.selections.SelectionManager;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.minefortress.MineFortressConstants.PICK_DISTANCE;

@Mixin(GameRenderer.class)
public abstract class FortressGameRendererMixin implements FortressGameRenderer {

    @Shadow
    public abstract Camera getCamera();

    @Shadow
    private double getFov(Camera camera, float f, boolean b) {return 0.0;}

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract void tick();

    @Shadow public abstract void reset();

    @Override
    public double getFov(float f, boolean b) {
        return this.getFov(this.getCamera(), f, b);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        final SelectionManager selectionManager = fortressClient.getSelectionManager();
        if(fortressClient.isFortressGamemode())  {
            if(this.client.crosshairTarget != null && this.client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) this.client.crosshairTarget;

                final FortressClientManager fortressClientManager = fortressClient.getFortressClientManager();
                final var fightSelectionManager = fortressClientManager
                        .getFightManager()
                        .getSelectionManager();
                if(fortressClientManager.isInCombat()) {
                    resetSelection(selectionManager);
                    if(fightSelectionManager.isSelectionStarted()) {
                        final var mouse = client.mouse;
                        final var crosshairTarget = client.crosshairTarget;
                        Vec3d pos;
                        if(crosshairTarget instanceof BlockHitResult) {
                            pos = crosshairTarget.getPos();
                        } else if(crosshairTarget instanceof EntityHitResult) {
                            pos = ((EntityHitResult) crosshairTarget).getEntity().getPos();
                        } else {
                            pos = null;
                        }
                        if(pos != null) {
                            fightSelectionManager.updateSelection(mouse.getX(), mouse.getY(), pos);
                        }
                    }
                    return;
                } else {
                    fightSelectionManager.resetSelection();
                }

                if(fortressClientManager.isCenterNotSet()) {
                    resetSelection(selectionManager);
                    fortressClientManager.updateRenderer(client.worldRenderer);
                    return;
                }

                final ClientBlueprintManager clientBlueprintManager = fortressClient.getBlueprintManager();
                if(clientBlueprintManager.hasSelectedBlueprint()) {
                    resetSelection(selectionManager);
                    return;
                }

                selectionManager.tickSelectionUpdate(blockHitResult.getBlockPos(), blockHitResult.getSide());
            }
        } else {
            resetSelection(selectionManager);
        }
    }

    private void resetSelection(SelectionManager selectionManager) {
        if (selectionManager.isSelecting()) {
            selectionManager.resetSelection();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        if(client.currentScreen == null && fortressClient.isFortressGamemode())
            fortressClient.getFortressHud().render(new MatrixStack(), tickDelta);
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d updateTargetedEntityGetRotation(Entity instance, float tickDelta) {
        if(instance instanceof ClientPlayerEntity player && ModUtils.isFortressGamemode(player)) {
            return CameraTools.getMouseBasedViewVector(MinecraftClient.getInstance(), player.getPitch(), player.getYaw());
        } else {
            return instance.getRotationVec(tickDelta);
        }
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D", ordinal = 1))
    public double updateTargetedEntityRedirectDistanceToEntity(Vec3d instance, Vec3d vec) {
        final double realDistance = instance.squaredDistanceTo(vec);

        if(!ModUtils.isClientInFortressGamemode()) return realDistance;
        if(realDistance > PICK_DISTANCE * PICK_DISTANCE) {
            return realDistance;
        } else {
            return 1;
        }
    }

}
