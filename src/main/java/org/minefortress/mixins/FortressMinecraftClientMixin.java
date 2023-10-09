package org.minefortress.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.jetbrains.annotations.Nullable;
import org.minefortress.MineFortressMod;
import org.minefortress.blueprints.interfaces.IBlockDataProvider;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import net.remmintan.panama.renderer.BlueprintRenderer;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.fight.influence.ClientInfluenceManager;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.FortressState;
import org.minefortress.fortress.automation.areas.AreasClientManager;
import org.minefortress.interfaces.ITasksInformationHolder;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.professions.hire.ClientHireHandler;
import org.minefortress.professions.hire.HireInfo;
import org.minefortress.renderer.FortressRenderLayer;
import org.minefortress.renderer.gui.blueprints.BlueprintsPauseScreen;
import org.minefortress.renderer.gui.hire.HirePawnScreen;
import org.minefortress.renderer.gui.hud.FortressHud;
import org.minefortress.selections.SelectionManager;
import org.minefortress.selections.renderer.ISelectionInfoProvider;
import org.minefortress.selections.renderer.ISelectionModelBuilderInfoProvider;
import net.remmintan.panama.renderer.CampfireRenderer;
import net.remmintan.panama.renderer.SelectionRenderer;
import org.minefortress.selections.renderer.tasks.ITasksModelBuilderInfoProvider;
import org.minefortress.selections.renderer.tasks.ITasksRenderInfoProvider;
import net.remmintan.panama.renderer.TasksRenderer;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.Map.entry;

@Mixin(MinecraftClient.class)
public abstract class FortressMinecraftClientMixin extends ReentrantThreadExecutor<Runnable> implements FortressMinecraftClient {

    @Unique
    private SelectionManager selectionManager;
    @Unique
    private FortressHud fortressHud;
    @Unique
    private FortressClientManager fortressClientManager;
    @Unique
    private final BlockBufferBuilderStorage blockBufferBuilderStorage = new BlockBufferBuilderStorage();
    @Unique
    private ClientBlueprintManager clientBlueprintManager;
    @Unique
    private ClientInfluenceManager influenceManager;
    @Unique
    private BlueprintRenderer blueprintRenderer;
    @Unique
    private CampfireRenderer campfireRenderer;
    @Unique
    private SelectionRenderer selectionRenderer;
    @Unique
    private TasksRenderer tasksRenderer;
    @Unique
    private AreasClientManager areasClientManager;

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

    public FortressMinecraftClientMixin(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(RunArgs args, CallbackInfo ci) {
        final MinecraftClient client = (MinecraftClient) (Object) this;

        this.selectionManager = new SelectionManager(client);
        this.fortressHud = new FortressHud(client);
        this.fortressClientManager = new FortressClientManager();
        this.areasClientManager = new AreasClientManager();

        clientBlueprintManager = new ClientBlueprintManager(client);
        influenceManager = new ClientInfluenceManager(client);


        blueprintRenderer = new BlueprintRenderer(this::getProperBlockDataProviderBasedOnState, client, blockBufferBuilderStorage);
        campfireRenderer = new CampfireRenderer(client, blockBufferBuilderStorage);
        Map<RenderLayer, BufferBuilder> selectionBufferBuilderStorage = Map.ofEntries(
                entry(RenderLayer.getLines(), new BufferBuilder(256)),
                entry(FortressRenderLayer.getLinesNoDepth(), new BufferBuilder(256))
        );

        final Supplier<ISelectionInfoProvider> selectInfProvSup = () ->
                ModUtils.getFortressClientManager().getState() == FortressState.BUILD ?
                        ModUtils.getSelectionManager() : ModUtils.getAreasClientManager();

        final Supplier<ISelectionModelBuilderInfoProvider> selModBuildInfProv = () ->
                ModUtils.getFortressClientManager().getState() == FortressState.BUILD ?
                        ModUtils.getSelectionManager() : ModUtils.getAreasClientManager();

        selectionRenderer = new SelectionRenderer(
                client,
                selectionBufferBuilderStorage,
                blockBufferBuilderStorage,
                selectInfProvSup,
                selModBuildInfProv
        );

        final Supplier<ITasksRenderInfoProvider> clientTasksHolderSupplier = () -> {
            if(fortressClientManager.getState() == FortressState.AREAS_SELECTION)
                return areasClientManager.getSavedAreasHolder();

            final ITasksInformationHolder fortressWorld = (ITasksInformationHolder) this.world;
            if(fortressWorld == null) return null;
            return fortressWorld.get_ClientTasksHolder();
        };

        final Supplier<ITasksModelBuilderInfoProvider> clientBlueprintManagerSupplier = () -> {
            if(fortressClientManager.getState() == FortressState.AREAS_SELECTION)
                return areasClientManager.getSavedAreasHolder();

            final ITasksInformationHolder fortressWorld = (ITasksInformationHolder) this.world;
            if(fortressWorld == null) return null;
            return fortressWorld.get_ClientTasksHolder();
        };

        tasksRenderer = new TasksRenderer(client,
                selectionBufferBuilderStorage.get(RenderLayer.getLines()),
                clientTasksHolderSupplier,
                clientBlueprintManagerSupplier);
    }

    @Override
    public SelectionManager get_SelectionManager() {
        return selectionManager;
    }

    @Override
    public boolean is_FortressGamemode() {
        return this.interactionManager != null && this.interactionManager.getCurrentGameMode() == MineFortressMod.FORTRESS;
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onInventoryOpened()V", shift = At.Shift.BEFORE))
    private void handleInputEvents(CallbackInfo ci) {
        if(this.interactionManager != null && this.interactionManager.getCurrentGameMode() == MineFortressMod.FORTRESS) {
            if(this.options.sprintKey.isPressed()) {
                if(this.get_BlueprintManager().isSelecting()) {
                    this.get_BlueprintManager().rotateSelectedStructureClockwise();
                } else {
                    this.get_SelectionManager().moveSelectionUp();
                }
            }
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void setScreenMix(Screen screen, CallbackInfo ci) {
        if(is_FortressGamemode()) {
            if(this.options.sprintKey.isPressed() && screen instanceof InventoryScreen) {
                ci.cancel();
            }
        }
    }

    @Inject(method="doItemPick", at=@At("HEAD"), cancellable = true)
    public void doItemPick(CallbackInfo ci) {
        if(this.is_FortressGamemode()) {
            ci.cancel();
        }
    }

    @Override
    public FortressHud get_FortressHud() {
        return fortressHud;
    }

    @Override
    public FortressClientManager get_FortressClientManager() {
        return fortressClientManager;
    }

    @Override
    public BlockPos get_HoveredBlockPos() {
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
            final boolean localServer = this.isIntegratedServerRunning() && (this.server == null || !this.server.isRemote());
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
    public BlueprintRenderer get_BlueprintRenderer() {
        return blueprintRenderer;
    }

    @Override
    public CampfireRenderer get_CampfireRenderer() {
        return campfireRenderer;
    }

    @Override
    public SelectionRenderer get_SelectionRenderer() {
        return selectionRenderer;
    }

    @Override
    public TasksRenderer get_TasksRenderer() {
        return tasksRenderer;
    }

    @Override
    public ClientBlueprintManager get_BlueprintManager() {
        return this.clientBlueprintManager;
    }

    @Override
    public AreasClientManager get_AreasClientManager() {
        return this.areasClientManager;
    }

    @Override
    public ClientInfluenceManager get_InfluenceManager() {
        return this.influenceManager;
    }


    @Override
    public void open_HireScreen(MinecraftClient client, String screenName, Map<String, HireInfo> professions) {
        final var handler = new ClientHireHandler(screenName, professions);
        final var screen = new HirePawnScreen(handler);
        client.setScreen(screen);
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void close(CallbackInfo ci) {
        this.blueprintRenderer.close();
        this.campfireRenderer.close();
        this.selectionRenderer.close();
    }

    @Unique
    private IBlockDataProvider getProperBlockDataProviderBasedOnState() {
        if(fortressClientManager.getState() == FortressState.COMBAT) {
            return this.get_InfluenceManager().getBlockDataProvider();
        }

        return this.get_BlueprintManager().getBlockDataProvider();
    }
}
