package org.minefortress.mixins.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import org.minefortress.interfaces.FortressGameRenderer;
import org.minefortress.renderer.CameraTools;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    private double getFov(Camera camera, float f, boolean b) {
        return 0.0;
    }

    @Shadow @Final MinecraftClient client;

    @Shadow public abstract void tick();

    @Shadow public abstract void reset();

    @Shadow public abstract MinecraftClient getClient();

    @Override
    public double get_Fov(float f, boolean b) {
        return this.getFov(this.getCamera(), f, b);
    }

    @Unique
    private static void resetAllSelectionManagers() {
        ClientModUtils.getSelectionManager().resetSelection();
        ClientModUtils.getAreasClientManager().resetSelection();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        final var provider = (IClientManagersProvider) this.client;
        final var selectionManager = provider.get_SelectionManager();
        final var clientFortressManager = provider.get_ClientFortressManager();
        final var pawnsSelectionManager = provider.get_PawnsSelectionManager();
        final var areasClientManager = ClientModUtils.getAreasClientManager();

        if (FortressGamemodeUtilsKt.isClientInFortressGamemode()) {
            final var clientState = clientFortressManager.getState();
            if(clientState == FortressState.COMBAT || clientState == FortressState.BUILD_SELECTION) {
                pawnsSelectionManager.updateSelection(client.mouse);
            }

            if(client.crosshairTarget instanceof BlockHitResult blockHitResult) {
                final IClientBlueprintManager clientBlueprintManager = provider.get_BlueprintManager();
                if(clientBlueprintManager.isSelecting()) {
                    resetAllSelectionManagers();
                    return;
                }

                if(clientState == FortressState.AREAS_SELECTION) {
                    areasClientManager.updateSelection(blockHitResult);
                } else {
                    areasClientManager.resetSelection();
                }

                if(clientState == FortressState.BUILD_EDITING) {
                    selectionManager.tickSelectionUpdate(blockHitResult.getBlockPos(), blockHitResult.getSide());
                } else {
                    selectionManager.resetSelection();
                }
            }
        } else {
            resetAllSelectionManagers();
        }
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d updateTargetedEntityGetRotation(Entity instance, float tickDelta) {
        if (instance instanceof ClientPlayerEntity player && FortressGamemodeUtilsKt.isFortressGamemode(player)) {
            final var mouse = client.mouse;
            return CameraTools.getMouseBasedViewVector(client, mouse.getX(), mouse.getY());
        } else {
            return instance.getRotationVec(tickDelta);
        }
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D", ordinal = 1))
    public double updateTargetedEntityRedirectDistanceToEntity(Vec3d instance, Vec3d vec) {
        final double realDistance = instance.squaredDistanceTo(vec);

        if (!FortressGamemodeUtilsKt.isClientInFortressGamemode()) return realDistance;
        if(realDistance > PICK_DISTANCE * PICK_DISTANCE) {
            return realDistance;
        } else {
            return 1;
        }
    }

}
