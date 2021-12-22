package org.minefortress.mixins.network;

import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.minefortress.MineFortressConstants.PICK_DISTANCE;

@Mixin(ServerPlayNetworkHandler.class)
public class FortressServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Shadow private @Nullable Vec3d requestedTeleportPos;

    @Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target="Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        ServerWorld serverWorld = this.player.getServerWorld();
        Hand hand = packet.getHand();
        ItemStack itemStack = this.player.getStackInHand(hand);
        BlockHitResult blockHitResult = packet.getBlockHitResult();
        BlockPos blockPos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getSide();
        int i = this.player.world.getTopY();
        if (blockPos.getY() < i) {
            if (this.requestedTeleportPos == null && this.player.squaredDistanceTo((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) < PICK_DISTANCE*PICK_DISTANCE && serverWorld.canPlayerModifyAt(this.player, blockPos)) {
                ActionResult actionResult = this.player.interactionManager.interactBlock(this.player, serverWorld, itemStack, hand, blockHitResult);
                if (direction == Direction.UP && !actionResult.isAccepted() && blockPos.getY() >= i - 1 && FortressServerPlayNetworkHandlerMixin.canPlace(this.player, itemStack)) {
                    MutableText text = new TranslatableText("build.tooHigh", i - 1).formatted(Formatting.RED);
                    this.player.sendMessage(text, MessageType.GAME_INFO, Util.NIL_UUID);
                } else if (actionResult.shouldSwingHand()) {
                    this.player.swingHand(hand, true);
                }
            }
            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(serverWorld, blockPos));
            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(serverWorld, blockPos.offset(direction)));
            ci.cancel();
        }
    }

    private static boolean canPlace(ServerPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return (item instanceof BlockItem || item instanceof BucketItem) && !player.getItemCooldownManager().isCoolingDown(item);
    }

}
