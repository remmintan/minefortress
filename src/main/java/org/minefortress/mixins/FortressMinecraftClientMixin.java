package org.minefortress.mixins;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.minefortress.renderer.CameraManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.selections.SelectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class FortressMinecraftClientMixin extends ReentrantThreadExecutor<Runnable> implements FortressMinecraftClient {

    private SelectionManager selectionManager;
    private CameraManager cameraManager;

    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    public ClientPlayerInteractionManager interactionManager;

    @Shadow @Final public Mouse mouse;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    public FortressMinecraftClientMixin(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructorHead(RunArgs args, CallbackInfo ci) {
        this.selectionManager = new SelectionManager((MinecraftClient)(Object)this);
        this.cameraManager = new CameraManager((MinecraftClient)(Object)this);
    }

    @Override
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public boolean isNotFortressGamemode() {
        return this.interactionManager == null || this.interactionManager.getCurrentGameMode() != ClassTinkerers.getEnum(GameMode.class, "FORTRESS");
    }

    @Override
    public boolean isFortressGamemode() {
        return !isNotFortressGamemode();
    }

    @Inject(method="render", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;updateMouse()V"))
    public void render(boolean tick, CallbackInfo ci) {
        final boolean middleMouseButtonIsDown = options.keyPickItem.isPressed();
        if(!isNotFortressGamemode()) { // is fortress
            if(middleMouseButtonIsDown) {
                if(!mouse.isCursorLocked())
                    mouse.lockCursor();
            } else {
                if(mouse.isCursorLocked())
                    mouse.unlockCursor();
            }
        }

        if(!isNotFortressGamemode() && !middleMouseButtonIsDown) {
            if(player != null) {
                float xRot = player.getPitch();
                float yRot = player.getYaw();
                if(!cameraManager.isNeededRotSet() && (xRot != 0 || yRot != 0)) {
                    cameraManager.setRot(xRot, yRot);
                }

                if(cameraManager.isNeededRotSet())
                    this.cameraManager.updateCameraPosition();
            }
        }

        if (isNotFortressGamemode() || middleMouseButtonIsDown) {
            if(player != null) {
                this.cameraManager.setRot(player.getPitch(), player.getYaw());
            }
        }
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onInventoryOpened()V", shift = At.Shift.BEFORE))
    private void handleInputEvents(CallbackInfo ci) {
        if(this.interactionManager != null && this.interactionManager.getCurrentGameMode() == ClassTinkerers.getEnum(GameMode.class, "FORTRESS")) {
            if(this.options.keySprint.isPressed()) {
                this.getSelectionManager().moveSelectionUp();
            }
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void setScreen(Screen screen, CallbackInfo ci) {
        if(this.interactionManager != null && this.interactionManager.getCurrentGameMode() == ClassTinkerers.getEnum(GameMode.class, "FORTRESS")) {
            if(this.options.keySprint.isPressed() && screen instanceof InventoryScreen) {
                ci.cancel();
            }
        }
    }

}
