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
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.blueprints.renderer.BlueprintRenderer;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.ServerboundSetTickSpeedPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.FortressCameraManager;
import org.minefortress.renderer.gui.ChooseModeScreen;
import org.minefortress.renderer.gui.FortressHud;
import org.minefortress.renderer.gui.blueprints.BlueprintsPauseScreen;
import org.minefortress.selections.SelectionManager;
import org.minefortress.selections.renderer.campfire.CampfireRenderer;
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

    private FortressClientManager fortressClientManager;

    private final BlockBufferBuilderStorage blockBufferBuilderStorage = new BlockBufferBuilderStorage();

    private ClientBlueprintManager clientBlueprintManager;
    private BlueprintRenderer blueprintRenderer;
    private CampfireRenderer campfireRenderer;

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

    @Shadow public abstract @Nullable IntegratedServer getServer();

    @Shadow public abstract boolean isIntegratedServerRunning();

    @Shadow private @Nullable IntegratedServer server;

    @Shadow @Final private SoundManager soundManager;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow @Nullable public Screen currentScreen;

    private int ticksSpeed = 1;

    public FortressMinecraftClientMixin(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(RunArgs args, CallbackInfo ci) {
        final MinecraftClient client = (MinecraftClient) (Object) this;

        this.selectionManager = new SelectionManager(client);
        this.fortressCameraManager = new FortressCameraManager(client);
        this.fortressHud = new FortressHud(client);
        this.fortressClientManager = new FortressClientManager();

        clientBlueprintManager = new ClientBlueprintManager(client);
        blueprintRenderer = new BlueprintRenderer(clientBlueprintManager.getBlockDataManager(), client, blockBufferBuilderStorage);
        campfireRenderer  = new CampfireRenderer(client, blockBufferBuilderStorage);
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

        if(isFortressGamemode() && !middleMouseButtonIsDown) {
            if(player != null) {
                this.fortressCameraManager.updateCameraPosition();
            }
        }

        if ((isNotFortressGamemode() || middleMouseButtonIsDown) && this.world!=null && this.world.getRegistryKey() != BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY) {
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
    public void setScreenMix(Screen screen, CallbackInfo ci) {
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
        if(this.fortressClientManager.gamemodeNeedsInitialization() && !(this.currentScreen instanceof ChooseModeScreen)) {
            this.setScreen(new ChooseModeScreen());
        }
    }

    @Override
    public FortressHud getFortressHud() {
        return fortressHud;
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

    @Inject(method = "openPauseMenu", at = @At("HEAD"), cancellable = true)
    public void openPauseMenu(boolean pause, CallbackInfo ci) {
        if(this.world != null && this.world.getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY) {
            final boolean localServer = this.isIntegratedServerRunning() && !this.server.isRemote();
            if (localServer) {
                this.setScreen(new BlueprintsPauseScreen(!pause));
                this.soundManager.pauseAll();
            } else {
                this.setScreen(new BlueprintsPauseScreen(true));
            }
            ci.cancel();
        }
    }

    @Override
    public BlueprintRenderer getBlueprintRenderer() {
        return blueprintRenderer;
    }

    @Override
    public CampfireRenderer getCampfireRenderer() {
        return campfireRenderer;
    }

    @Override
    public ClientBlueprintManager getBlueprintManager() {
        return this.clientBlueprintManager;
    }

    @Override
    public boolean isSupporter() {
        return true;
    }

    @Override
    public void setTicksSpeed(int ticksSpeed) {
        final ServerboundSetTickSpeedPacket packet = new ServerboundSetTickSpeedPacket(ticksSpeed);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SET_TICKS_SPEED, packet);
        this.ticksSpeed = ticksSpeed;
    }

    @Override
    public int getTicksSpeed() {
        return this.ticksSpeed;
    }
}
