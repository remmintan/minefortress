package org.minefortress.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.remmintan.gobi.SelectionManager;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.config.MineFortressClientConfig;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlockDataProvider;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintsImportExportManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BlueprintsDimensionUtilsKt;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IClientBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.client.IFortressCenterManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientPawnsSelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.renderers.IGuiBlueprintsRenderer;
import net.remmintan.mods.minefortress.core.interfaces.renderers.IRenderersProvider;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionInfoProvider;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionModelBuilderInfoProvider;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksModelBuilderInfoProvider;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksRenderInfoProvider;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.DisclaimerScreen;
import net.remmintan.panama.renderer.BlueprintRenderer;
import net.remmintan.panama.renderer.FortressRenderLayer;
import net.remmintan.panama.renderer.SelectionRenderer;
import net.remmintan.panama.renderer.TasksRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.manager.BlueprintsImportExportManager;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.fight.ClientPawnsSelectionManager;
import org.minefortress.fortress.ClientFortressManager;
import org.minefortress.fortress.FortressCenterManager;
import org.minefortress.fortress.automation.areas.AreasClientManager;
import org.minefortress.fortress.buildings.ClientBuildingsManager;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.renderer.gui.blueprints.BlueprintsPauseScreen;
import org.minefortress.renderer.gui.hud.FortressHud;
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
public abstract class FortressMinecraftClientMixin extends ReentrantThreadExecutor<Runnable>
        implements IFortressMinecraftClient, IClientManagersProvider, IRenderersProvider {

    @Unique
    private ISelectionManager selectionManager;
    @Unique
    private FortressHud fortressHud;
    @Unique
    private ClientFortressManager clientFortressManager;
    @Unique
    private final BlockBufferBuilderStorage blockBufferBuilderStorage = new BlockBufferBuilderStorage();
    @Unique
    private ClientBlueprintManager clientBlueprintManager;
    @Unique
    private IFortressCenterManager fortressCenterManager;

    @Unique
    private BlueprintRenderer blueprintRenderer;
    @Unique
    private SelectionRenderer selectionRenderer;
    @Unique
    private TasksRenderer tasksRenderer;
    @Unique
    private AreasClientManager areasClientManager;
    @Unique
    private final IClientPawnsSelectionManager pawnsSelectionManager = new ClientPawnsSelectionManager();
    @Unique
    private final IClientBuildingsManager buildingsManager = new ClientBuildingsManager();
    @Unique
    private final IBlueprintsImportExportManager blueprintsImportExportManager = new BlueprintsImportExportManager();

    @Unique
    private FortressGamemode gamemode;

    @Shadow @Final public Mouse mouse;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow @Nullable public ClientWorld world;

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
        this.clientFortressManager = new ClientFortressManager();
        this.fortressCenterManager = new FortressCenterManager();
        this.areasClientManager = new AreasClientManager();

        clientBlueprintManager = new ClientBlueprintManager(client);


        blueprintRenderer = new BlueprintRenderer(this::getProperBlockDataProviderBasedOnState, client, blockBufferBuilderStorage);
        Map<RenderLayer, BufferBuilder> selectionBufferBuilderStorage = Map.ofEntries(
                entry(RenderLayer.getLines(), new BufferBuilder(256)),
                entry(FortressRenderLayer.getLinesNoDepth(), new BufferBuilder(256))
        );

        final var provider = ClientModUtils.getManagersProvider();
        final var manager = provider.get_ClientFortressManager();

        final Supplier<Boolean> isInBuildState = () -> manager.getState() == FortressState.BUILD_SELECTION || manager.getState() == FortressState.BUILD_EDITING;
        final Supplier<ISelectionInfoProvider> selectInfProvSup = () ->
                isInBuildState.get() ? ClientModUtils.getSelectionManager() : ClientModUtils.getAreasClientManager();

        final Supplier<ISelectionModelBuilderInfoProvider> selModBuildInfProv = () ->
                isInBuildState.get() ? ClientModUtils.getSelectionManager() : ClientModUtils.getAreasClientManager();

        selectionRenderer = new SelectionRenderer(
                client,
                selectionBufferBuilderStorage,
                blockBufferBuilderStorage,
                selectInfProvSup,
                selModBuildInfProv
        );

        final Supplier<ITasksRenderInfoProvider> clientTasksHolderSupplier = () -> {
            if(clientFortressManager.getState() == FortressState.AREAS_SELECTION)
                return areasClientManager.getSavedAreasHolder();

            final ITasksInformationHolder fortressWorld = (ITasksInformationHolder) this.world;
            if(fortressWorld == null) return null;
            return fortressWorld.get_ClientTasksHolder();
        };

        final Supplier<ITasksModelBuilderInfoProvider> clientBlueprintManagerSupplier = () -> {
            if(clientFortressManager.getState() == FortressState.AREAS_SELECTION)
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
    public ISelectionManager get_SelectionManager() {
        return selectionManager;
    }


    @Inject(method="doItemPick", at=@At("HEAD"), cancellable = true)
    public void doItemPick(CallbackInfo ci) {
        if (FortressGamemodeUtilsKt.isClientInFortressGamemode()) {
            ci.cancel();
        }
    }

    @Override
    public FortressHud get_FortressHud() {
        return fortressHud;
    }

    @Override
    public ClientFortressManager get_ClientFortressManager() {
        return clientFortressManager;
    }

    @Override
    public IFortressCenterManager get_FortressCenterManager() {
        return fortressCenterManager;
    }

    @Inject(method = "openGameMenu", at = @At("HEAD"), cancellable = true)
    public void openGameMenu(boolean pauseOnly, CallbackInfo ci) {
        if (this.world != null && this.world.getRegistryKey() == BlueprintsDimensionUtilsKt.getBLUEPRINT_DIMENSION_KEY()) {
            final boolean localServer = this.isIntegratedServerRunning() && (this.server == null || !this.server.isRemote());
            if (localServer) {
                this.setScreen(new BlueprintsPauseScreen(!pauseOnly));
                this.soundManager.pauseAll();
            } else {
                this.setScreen(new BlueprintsPauseScreen(true));
            }
            ci.cancel();
        }

        final var pawnsSelection = ClientModUtils.getManagersProvider().get_PawnsSelectionManager();
        if(!pawnsSelection.getSelectedPawnsIds().isEmpty()) {
            pawnsSelection.resetSelection();
            ci.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void setScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof SelectWorldScreen) {
            final var disclaimerAcknowledged = MineFortressClientConfig.INSTANCE.getDisclaimerAcknowledged();
            if (!disclaimerAcknowledged) {
                this.setScreen(new DisclaimerScreen(screen));
                ci.cancel();
            }
        }

        if (FortressGamemodeUtilsKt.isClientInFortressGamemode()) {
            if (screen instanceof InventoryScreen && (selectionManager.isSelecting() || clientBlueprintManager.isSelecting())) {
                ci.cancel();
            }
        }
    }


    @Override
    public BlueprintRenderer get_BlueprintRenderer() {
        return blueprintRenderer;
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
    public IClientPawnsSelectionManager get_PawnsSelectionManager() {
        return pawnsSelectionManager;
    }

    @Override
    public IClientBuildingsManager get_BuildingsManager() {
        return buildingsManager;
    }

    @Override
    public IBlueprintsImportExportManager get_BlueprintsImportExportManager() {
        return blueprintsImportExportManager;
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void close(CallbackInfo ci) {
        this.blueprintRenderer.close();
        this.selectionRenderer.close();
    }

    @Unique
    private IBlockDataProvider getProperBlockDataProviderBasedOnState() {
        return this.get_BlueprintManager().getBlockDataProvider();
    }

    @NotNull
    @Override
    public IGuiBlueprintsRenderer get_GuiBlueprintsRenderer() {
        return get_BlueprintRenderer();
    }

    @Override
    public FortressGamemode get_fortressGamemode() {
        return gamemode;
    }

    @Override
    public void set_fortressGamemode(FortressGamemode fortressGamemode) {
        this.gamemode = fortressGamemode;
    }
}
