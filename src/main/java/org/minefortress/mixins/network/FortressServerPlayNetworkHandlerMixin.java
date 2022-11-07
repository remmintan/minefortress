package org.minefortress.mixins.network;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.minefortress.MineFortressMod;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.registries.FortressEntities;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.minefortress.MineFortressConstants.PICK_DISTANCE;
import static org.minefortress.MineFortressConstants.PICK_DISTANCE_FLOAT;

@Mixin(ServerPlayNetworkHandler.class)
public class FortressServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Shadow private @Nullable Vec3d requestedTeleportPos;

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target="Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        ServerWorld serverWorld = this.player.getWorld();
        Hand hand = packet.getHand();
        ItemStack itemStack = this.player.getStackInHand(hand);
        BlockHitResult blockHitResult = packet.getBlockHitResult();
        BlockPos blockPos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getSide();
        int i = this.player.world.getTopY();
        if (blockPos.getY() < i) {
            if (this.requestedTeleportPos == null && this.player.squaredDistanceTo((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) < PICK_DISTANCE*PICK_DISTANCE && serverWorld.canPlayerModifyAt(this.player, blockPos)) {
                this.addCustomNbtToStack(itemStack);
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

    @Redirect(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"))
    public double playerInteractEntitySquareDistance(ServerPlayerEntity instance, Entity entity) {
        final double realDistance = instance.squaredDistanceTo(entity);
        if(ModUtils.isFortressGamemode(instance)) {
            if(Math.sqrt(realDistance) < PICK_DISTANCE) return 1;
        }
        return realDistance;
    }

    private static boolean canPlace(ServerPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return (item instanceof BlockItem || item instanceof BucketItem) && !player.getItemCooldownManager().isCoolingDown(item);
    }

    private void addCustomNbtToStack(ItemStack stack) {
        if(stack == null) return;
        if(stack.getItem() instanceof SpawnEggItem eggItem) {
            final EntityType<?> entityType = eggItem.getEntityType(stack.getNbt());
            if(!FortressEntities.isFortressAwareEntityType(entityType)) return;
            if(stack.getNbt() == null) stack.setNbt(new NbtCompound());

            final var fortressServer = (FortressServer) this.server;
            final var fortressManager = fortressServer.getFortressModServerManager().getByPlayer(player);

            final NbtCompound nbt = stack.getNbt();
            nbt.putUuid("fortressUUID", fortressManager.getId());
            final BlockPos fortressCenter = fortressManager.getFortressCenter();
            if(fortressCenter != null) {
                nbt.putInt("centerX", fortressCenter.getX());
                nbt.putInt("centerY", fortressCenter.getY());
                nbt.putInt("centerZ", fortressCenter.getZ());
            }
        }
    }

}
