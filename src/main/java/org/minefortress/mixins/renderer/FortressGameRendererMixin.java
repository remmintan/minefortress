package org.minefortress.mixins.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.interfaces.FortressGameRenderer;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.selections.SelectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class FortressGameRendererMixin implements FortressGameRenderer {

    @Shadow
    public abstract Camera getCamera();

    @Shadow
    private double getFov(Camera camera, float f, boolean b) {return 0.0;}

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract void tick();

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
                final BlueprintManager blueprintManager = fortressClient.getBlueprintManager();
                if(blueprintManager.hasSelectedBlueprint()) {
                    resetSelection(selectionManager);
                    blueprintManager.tickUpdate(blockHitResult.getBlockPos());
                } else {
                    selectionManager.tickSelectionUpdate(blockHitResult.getBlockPos(), blockHitResult.getSide());
                }
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

}
