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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.BlueprintMetadataManager;
import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.renderer.FortressCameraManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.FortressHud;
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
    private FortressCameraManager fortressCameraManager;
    private FortressHud fortressHud;
    private BlueprintMetadataManager blueprintMetadataManager;
    private FortressClientManager fortressClientManager;

    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    public ClientPlayerInteractionManager interactionManager;

    @Shadow @Final public Mouse mouse;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow @Nullable public ClientWorld world;

    @Shadow @Nullable public HitResult crosshairTarget;

    public FortressMinecraftClientMixin(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(RunArgs args, CallbackInfo ci) {
        this.selectionManager = new SelectionManager((MinecraftClient)(Object)this);
        this.fortressCameraManager = new FortressCameraManager((MinecraftClient)(Object)this);
        this.fortressHud = new FortressHud((MinecraftClient)(Object)this);
        this.blueprintMetadataManager = new BlueprintMetadataManager((MinecraftClient)(Object)this);
        this.fortressClientManager = new FortressClientManager();
    }

    @Override
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }


    @Override
    public BlueprintMetadataManager getBlueprintMetadataManager() {
        return blueprintMetadataManager;
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

        if(isFortressGamemode() && !middleMouseButtonIsDown) {
            if(player != null) {
                this.fortressCameraManager.updateCameraPosition();
            }
        }

        if (isNotFortressGamemode() || middleMouseButtonIsDown) {
            if(player != null) {
                this.fortressCameraManager.setRot(player.getPitch(), player.getYaw());
            }
        }
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onInventoryOpened()V", shift = At.Shift.BEFORE))
    private void handleInputEvents(CallbackInfo ci) {
        if(this.interactionManager != null && this.interactionManager.getCurrentGameMode() == ClassTinkerers.getEnum(GameMode.class, "FORTRESS")) {
            if(this.options.keySprint.isPressed()) {
                if(this.getBlueprintManager().hasSelectedBlueprint()) {
                    this.getBlueprintManager().rotateSelectedStructureClockwise();
                } else {
                    this.getSelectionManager().moveSelectionUp();
                }
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

    @Inject(method="doItemPick", at=@At("HEAD"), cancellable = true)
    public void doItemPick(CallbackInfo ci) {
        if(this.isFortressGamemode()) {
            ci.cancel();
        }
    }

    @Inject(method="tick", at=@At("TAIL"))
    public void tick(CallbackInfo ci) {
        this.fortressHud.tick();
        this.fortressClientManager.tick(this);
    }

    @Override
    public FortressHud getFortressHud() {
        return fortressHud;
    }

    @Override
    public BlueprintManager getBlueprintManager() {
        final ClientWorld world = this.world;
        if(world != null) {
            return ((FortressClientWorld) world).getBlueprintManager();
        } else {
            throw new IllegalStateException("Client world is null");
        }
    }

    @Override
    public FortressClientManager getFortressClientManager() {
        return fortressClientManager;
    }

    @Override
    public BlockPos getHoveredBlockPos() {
        final HitResult hitResult = this.crosshairTarget;
        if(hitResult instanceof BlockHitResult) {
            return ((BlockHitResult) hitResult).getBlockPos();
        } else {
            return null;
        }
    }
}
