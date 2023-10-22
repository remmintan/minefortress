package org.minefortress.mixins.interaction;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.minefortress.MineFortressMod;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import org.minefortress.fortress.ClientFortressManager;
import net.remmintan.mods.minefortress.core.FortressState;
import org.minefortress.interfaces.IFortressMinecraftClient;
import net.remmintan.mods.minefortress.core.interfaces.client.IHoveredBlockProvider;
import org.minefortress.renderer.gui.fortress.ManageBuildingScreen;
import org.minefortress.selections.SelectionManager;
import org.minefortress.utils.BlockUtils;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.minefortress.MineFortressConstants.PICK_DISTANCE_FLOAT;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class FortressClientInteractionManagerMixin {

    @Unique
    private static final GameMode FORTRESS = MineFortressMod.FORTRESS;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract GameMode getCurrentGameMode();

    @Shadow
    private void syncSelectedSlot() {}

    @Inject(method = "setGameModes", at = @At("RETURN"))
    public void setGameModes(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci) {
        final IHoveredBlockProvider fortressClient = (IHoveredBlockProvider) this.client;
        final SelectionManager selectionManager = fortressClient.get_SelectionManager();
        if(selectionManager.isSelecting()) {
            selectionManager.resetSelection();
        }
        if(gameMode == FORTRESS) {
            setFortressMode();
        } else {
            unsetFortressMode();
        }
    }



    @Inject(method = "setGameMode", at = @At("RETURN"))
    public void setGameMode(GameMode gameMode, CallbackInfo ci) {
        if(gameMode == FORTRESS) {
            setFortressMode();
        } else {
            unsetFortressMode();
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    public void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(!ModUtils.isClientInFortressGamemode()) return;
        final IFortressMinecraftClient fortressClient = (IFortressMinecraftClient) this.client;
        final IClientBlueprintManager clientBlueprintManager = fortressClient.get_BlueprintManager();
        final ClientFortressManager fortressManager = fortressClient.get_FortressClientManager();

        if(fortressManager.getState() == FortressState.COMBAT) {
            final var influenceManager = ModUtils.getInfluenceManager();
            if(influenceManager.isSelecting()) {
                influenceManager.cancelSelectingInfluencePosition();
                cir.setReturnValue(false);
                return;
            }
            final var selectionManager = fortressManager.getFightManager().getSelectionManager();
            final var mouse = client.mouse;

            if(selectionManager.isSelecting())
                selectionManager.endSelection();
            else {
                if(selectionManager.hasSelected()) {
                    selectionManager.resetSelection();
                } else {
                    final var crosshairTarget = client.crosshairTarget;
                    if (crosshairTarget!=null)
                        selectionManager.startSelection(mouse.getX(), mouse.getY(), crosshairTarget.getPos());
                }
            }

            cir.setReturnValue(false);
            return;
        }

        if(fortressManager.getState() == FortressState.AREAS_SELECTION) {
            final var areasClientManager = ModUtils.getAreasClientManager();
            areasClientManager.select(client.crosshairTarget);
            cir.setReturnValue(false);
            return;
        }

        if(fortressManager.isSelectingColonist()){
            fortressManager.stopSelectingColonist();
            cir.setReturnValue(false);
            return;
        }

        if(fortressManager.isCenterNotSet()) {
            cir.setReturnValue(false);
            return;
        }

        if(clientBlueprintManager.isSelecting()) {
           clientBlueprintManager.clearStructure();
            cir.setReturnValue(false);
            return;
        }


        if(fortressManager.isBuildingHovered()) {
            openManageBuildingMenu(fortressManager);
            cir.setReturnValue(false);
            return;
        }

        fortressClient.get_SelectionManager().selectBlock(pos);
        cir.setReturnValue(false);
    }

    @Unique
    private static void openManageBuildingMenu(ClientFortressManager fortressManager) {
        fortressManager
                .getHoveredBuilding()
                .ifPresent(it -> {
                    final var manageBuildingScreen = new ManageBuildingScreen(it);
                    MinecraftClient.getInstance().setScreen(manageBuildingScreen);
                });
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    public void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(ModUtils.isClientInFortressGamemode())
            cir.setReturnValue(true);
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    public void interactEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(getCurrentGameMode() == FORTRESS) {
            final IFortressMinecraftClient fortressClient = (IFortressMinecraftClient) this.client;
            final ClientFortressManager fcm = fortressClient.get_FortressClientManager();
            if (fcm.getState() == FortressState.COMBAT) {
                final var fightManager = fcm.getFightManager();
                final var selectionManager = fightManager.getSelectionManager();
                if(selectionManager.isSelecting())
                    selectionManager.resetSelection();

                if(selectionManager.hasSelected()) {
                    fightManager.setTarget(entity);
                }
                cir.setReturnValue(ActionResult.FAIL);
                return;
            }
            if(entity instanceof LivingEntity) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(!ModUtils.isClientInFortressGamemode())return;
        syncSelectedSlot();
        BlockPos blockPos = hitResult.getBlockPos();
        final var world = this.client.world;
        if(world!= null && !world.getWorldBorder().contains(blockPos)) return;


        final IFortressMinecraftClient fortressClient = (IFortressMinecraftClient) this.client;
        final IClientBlueprintManager clientBlueprintManager = fortressClient.get_BlueprintManager();
        final ClientFortressManager fortressManager = fortressClient.get_FortressClientManager();

        if(fortressManager.getState() == FortressState.COMBAT) {
            final var influenceManager = ModUtils.getInfluenceManager();
            if(influenceManager.isSelecting()) {
                influenceManager.selectInfluencePosition();
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }
            updateFightSelection(hitResult, fortressManager);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if(fortressManager.getState() == FortressState.AREAS_SELECTION) {
            final var areasClientManager = ModUtils.getAreasClientManager();
            if(areasClientManager.isSelecting())
                areasClientManager.resetSelection();
            else
                areasClientManager.removeHovered();
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if(fortressManager.isCenterNotSet()) {
            fortressManager.setupFortressCenter();
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if(clientBlueprintManager.isSelecting()) {
            clientBlueprintManager.buildCurrentStructure();
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if(fortressManager.isBuildingHovered()){
            openManageBuildingMenu(fortressManager);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        final ItemStack stackInHand = player.getStackInHand(hand);
        Item item = stackInHand.getItem();
        ItemUsageContext useoncontext = new ItemUsageContext(player, hand, hitResult);
        final BlockState blockStateFromItem = BlockUtils.getBlockStateFromItem(item);
        if(blockStateFromItem != null) {
            clickBuild(useoncontext, blockStateFromItem);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }
        final SelectionManager selectionManager = fortressClient.get_SelectionManager();
        if(selectionManager.isSelecting()) {
            selectionManager.selectBlock(blockPos, null);
            cir.setReturnValue(ActionResult.SUCCESS);
        }

        if(stackInHand.isEmpty()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Unique
    private static void updateFightSelection(BlockHitResult hitResult, ClientFortressManager fortressManager) {
        final var fightManager = fortressManager.getFightManager();
        final var selectionManager = fightManager.getSelectionManager();
        if(selectionManager.isSelecting())
            selectionManager.resetSelection();

        if(selectionManager.hasSelected()) {
            fightManager.setTarget(hitResult);
        }
    }

    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    public void getReachDistance(CallbackInfoReturnable<Float> cir) {
        if(getCurrentGameMode()==FORTRESS)
            cir.setReturnValue(PICK_DISTANCE_FLOAT);
    }

    @Inject(method = "hasExtendedReach", at = @At("HEAD"), cancellable = true)
    public void hasExtendedReach(CallbackInfoReturnable<Boolean> cir) {
        if(getCurrentGameMode()==FORTRESS)
            cir.setReturnValue(false);
    }

    @Inject(method = "isFlyingLocked", at = @At("HEAD"), cancellable = true)
    public void isFlyingLocked(CallbackInfoReturnable<Boolean> cir) {
        if(getCurrentGameMode()==FORTRESS)
            cir.setReturnValue(true);
    }

    @Inject(method = "hasCreativeInventory", at = @At("HEAD"), cancellable = true)
    public void hasCreativeInventory(CallbackInfoReturnable<Boolean> cir) {
        if(getCurrentGameMode()==FORTRESS)
            cir.setReturnValue(true);
    }

    @Unique
    private void clickBuild(ItemUsageContext useOnContext, BlockState blockState) {
        BlockPos blockPos = useOnContext.getBlockPos();
        if(!BuildingHelper.canPlaceBlock(useOnContext.getWorld(), blockPos)){
            blockPos = blockPos.offset(useOnContext.getSide());
        }

        ((IHoveredBlockProvider)client).get_SelectionManager().selectBlock(blockPos, blockState);
    }

    @Unique
    private void setFortressMode() {
        client.mouse.unlockCursor();
        client.gameRenderer.setRenderHand(false);

        final TutorialManager tutorialManager = client.getTutorialManager();
        tutorialManager.setStep(TutorialStep.NONE);
    }

    @Unique
    private void unsetFortressMode() {
        client.mouse.lockCursor();
        client.gameRenderer.setRenderHand(true);
    }

}
