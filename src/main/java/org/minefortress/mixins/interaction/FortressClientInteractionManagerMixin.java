package org.minefortress.mixins.interaction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import org.minefortress.MineFortressMod;
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

    @Inject(method = "setGameModes", at = @At("RETURN"))
    public void setGameModes(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci) {
        final var fortressClient = (IClientManagersProvider) this.client;
        final var selectionManager = fortressClient.get_SelectionManager();
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
        final var fortressClient = (IClientManagersProvider) this.client;
        final var clientBlueprintManager = fortressClient.get_BlueprintManager();
        final var manager = fortressClient.get_ClientFortressManager();

        if(manager.isCenterNotSet()) {
            cir.setReturnValue(false);
            return;
        }

        if(manager.isBuildingHovered()) {
            openManageBuildingMenu(manager);
            cir.setReturnValue(false);
            return;
        }

        if(manager.getState() == FortressState.BUILD_SELECTION) {
            cir.setReturnValue(false);
            return;
        }

        if(manager.getState() == FortressState.AREAS_SELECTION) {
            final var areasClientManager = ModUtils.getAreasClientManager();
            areasClientManager.select(client.crosshairTarget);
            cir.setReturnValue(false);
            return;
        }

        if(clientBlueprintManager.isSelecting()) {
           clientBlueprintManager.clearStructure();
            cir.setReturnValue(false);
            return;
        }

        fortressClient.get_SelectionManager().selectBlock(pos);
        cir.setReturnValue(false);
    }

    @Unique
    private static void openManageBuildingMenu(IClientFortressManager fortressManager) {
        fortressManager
                .getHoveredBuilding()
                .ifPresent(it -> {
                    // TODO: open the new building screen
//                    final var manageBuildingScreen = new ManageBuildingScreen(it);
//                    MinecraftClient.getInstance().setScreen(manageBuildingScreen);
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
            final var provider = (IClientManagersProvider) this.client;
            final var manager = provider.get_ClientFortressManager();
            if (manager.getState() == FortressState.COMBAT) {
                final var fightManager = manager.getFightManager();
                final var targetedSelectionManager = provider.getTargetedSelectionManager();

                fightManager.setTarget(entity, targetedSelectionManager);
                cir.setReturnValue(ActionResult.FAIL);
                return;
            }
            if(entity instanceof LivingEntity) {
                cir.setReturnValue(ActionResult.FAIL);
            }
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
