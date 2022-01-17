package org.minefortress.mixins.interaction;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.selections.SelectionManager;
import org.minefortress.utils.BlockUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.minefortress.MineFortressConstants.PICK_DISTANCE_FLOAT;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class FortressInteractionManagerMixin {

    private static final GameMode FORTRESS = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");

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

    private void unsetFortressMode() {
        client.mouse.lockCursor();
        client.gameRenderer.setRenderHand(true);
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
        if(getCurrentGameMode() == FORTRESS) {
            final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
            final BlueprintManager blueprintManager = fortressClient.getBlueprintManager();
            if(blueprintManager.hasSelectedBlueprint()) {
               blueprintManager.clearStructure();
            } else {
                fortressClient.getSelectionManager().selectBlock(pos);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    public void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(getCurrentGameMode() == FORTRESS)
            cir.setReturnValue(true);
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void interactBlock(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(getCurrentGameMode() == FORTRESS) {
            syncSelectedSlot();
            BlockPos blockPos = hitResult.getBlockPos();
            if(world.getWorldBorder().contains(blockPos)) {
                final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
                final BlueprintManager blueprintManager = fortressClient.getBlueprintManager();
                if(blueprintManager.hasSelectedBlueprint()) {
                    blueprintManager.buildCurrentStructure(blockPos);
                    cir.setReturnValue(ActionResult.SUCCESS);
                } else {
                    Item item = player.getStackInHand(hand).getItem();
                    ItemUsageContext useoncontext = new ItemUsageContext(player, hand, hitResult);
                    final BlockState blockStateFromItem = BlockUtils.getBlockStateFromItem(item);
                    if(blockStateFromItem != null) {
                        final ActionResult returnValue = clickBuild(useoncontext, blockStateFromItem);
                        cir.setReturnValue(returnValue);
                        return;
                    }
                    final SelectionManager selectionManager = fortressClient.getSelectionManager();
                    if(selectionManager.isSelecting()) {
                        selectionManager.selectBlock(blockPos, null);
                        cir.setReturnValue(ActionResult.SUCCESS);
                    }
                }
            }
        }
    }

    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    public void getReachDistance(CallbackInfoReturnable<Float> cir) {
        if(getCurrentGameMode()==FORTRESS)
            cir.setReturnValue(PICK_DISTANCE_FLOAT);
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

    private ActionResult clickBuild(ItemUsageContext useOnContext, BlockState blockState) {
        BlockPos blockPos = useOnContext.getBlockPos().offset(useOnContext.getSide());

        ((FortressMinecraftClient)client).getSelectionManager().selectBlock(blockPos, blockState);
        return ActionResult.SUCCESS;
    }

    private void setFortressMode() {
        client.mouse.unlockCursor();
        client.gameRenderer.setRenderHand(false);
    }

}
