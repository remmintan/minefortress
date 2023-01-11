package org.minefortress.mixins.interaction;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
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
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.FortressState;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.professions.Profession;
import org.minefortress.selections.SelectionManager;
import org.minefortress.utils.BlockUtils;
import org.minefortress.utils.BuildingHelper;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.minefortress.MineFortressConstants.PICK_DISTANCE_FLOAT;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class FortressClientInteractionManagerMixin {

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
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        final SelectionManager selectionManager = fortressClient.getSelectionManager();
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
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        final ClientBlueprintManager clientBlueprintManager = fortressClient.getBlueprintManager();
        final FortressClientManager fortressManager = fortressClient.getFortressClientManager();

        if(fortressManager.getState() == FortressState.COMBAT) {
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
            final var result = areasClientManager.select(client.crosshairTarget);
            cir.setReturnValue(result);
            return;
        }

        if(fortressManager.isSelectingColonist()){
            fortressManager.stopSelectingColonist();
            cir.setReturnValue(false);
            return;
        }

        if(fortressManager.isCenterNotSet()) {
            cir.setReturnValue(true);
            return;
        }

        if(clientBlueprintManager.hasSelectedBlueprint()) {
           clientBlueprintManager.clearStructure();
            cir.setReturnValue(true);
            return;
        }

        if(fortressManager.isBuildingSelected()){
            final var hoveredBuilding = fortressManager.getHoveredBuilding();
            final var professionManager = fortressManager.getProfessionManager();
            professionManager
                    .getByRequirement(hoveredBuilding.getRequirementId())
                    .filter(Profession::isHireMenu)
                    .ifPresent(it -> professionManager.increaseAmount(professionManager.getIdByProfession(it), false));
            return;
        }

        fortressClient.getSelectionManager().selectBlock(pos);
        cir.setReturnValue(true);
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    public void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(ModUtils.isClientInFortressGamemode())
            cir.setReturnValue(true);
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    public void interactEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(getCurrentGameMode() == FORTRESS) {
            final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
            final FortressClientManager fcm = fortressClient.getFortressClientManager();
            if (fcm.getState() == FortressState.COMBAT) {
                final var fightManager = fcm.getFightManager();
                final var selectionManager = fightManager.getSelectionManager();
                if(selectionManager.isSelecting())
                    selectionManager.resetSelection();

                if(selectionManager.hasSelected()) {
                    fightManager.setTarget(entity);
                }
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void interactBlock(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(getCurrentGameMode() != FORTRESS)return;
        syncSelectedSlot();
        BlockPos blockPos = hitResult.getBlockPos();
        if(world.getWorldBorder().contains(blockPos)) return;


        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        final ClientBlueprintManager clientBlueprintManager = fortressClient.getBlueprintManager();
        final FortressClientManager fortressManager = fortressClient.getFortressClientManager();

        if(fortressManager.getState() == FortressState.COMBAT) {
            updateFightSelection(hitResult, fortressManager);
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if(fortressManager.getState() == FortressState.AREAS_SELECTION) {
            final var areasClientManager = ModUtils.getAreasClientManager();
            areasClientManager.resetSelection();
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if(fortressManager.isCenterNotSet()) {
            fortressManager.setupFortressCenter();
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if(clientBlueprintManager.hasSelectedBlueprint()) {
            clientBlueprintManager.buildCurrentStructure();
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        if(fortressManager.isBuildingSelected()){
            cir.setReturnValue(ActionResult.PASS);
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
        final SelectionManager selectionManager = fortressClient.getSelectionManager();
        if(selectionManager.isSelecting()) {
            selectionManager.selectBlock(blockPos, null);
            cir.setReturnValue(ActionResult.SUCCESS);
        }

        if(stackInHand.isEmpty()) {
            cir.setReturnValue(ActionResult.PASS);
        }

    }

    private static void updateFightSelection(BlockHitResult hitResult, FortressClientManager fortressManager) {
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

    private void clickBuild(ItemUsageContext useOnContext, BlockState blockState) {
        BlockPos blockPos = useOnContext.getBlockPos();
        if(!BuildingHelper.canPlaceBlock(useOnContext.getWorld(), blockPos)){
            blockPos = blockPos.offset(useOnContext.getSide());
        }

        ((FortressMinecraftClient)client).getSelectionManager().selectBlock(blockPos, blockState);
    }

    private void setFortressMode() {
        client.mouse.unlockCursor();
        client.gameRenderer.setRenderHand(false);

        final TutorialManager tutorialManager = client.getTutorialManager();
        tutorialManager.setStep(TutorialStep.NONE);
    }

    private void unsetFortressMode() {
        client.mouse.lockCursor();
        client.gameRenderer.setRenderHand(true);
    }

}
