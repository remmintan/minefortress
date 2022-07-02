package org.minefortress.mixins.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fortress.resources.client.FortressItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public abstract class FortressPacketByteBufMixin extends ByteBuf {

    @Shadow public abstract PacketByteBuf writeVarInt(int value);

    @Shadow public abstract PacketByteBuf writeNbt(@Nullable NbtCompound compound);

    @Shadow public abstract ByteBuf writeInt(int value);

    @Shadow public abstract int readVarInt();

    @Shadow @Nullable public abstract NbtCompound readNbt();

    @Shadow public abstract int readInt();

    @Inject(method = "writeItemStack", at = @At("HEAD"), cancellable = true)
    void writeItemsStack(ItemStack stack, CallbackInfoReturnable<PacketByteBuf> cir) {
        if (stack.isEmpty()) {
            this.writeBoolean(false);
        } else {
            this.writeBoolean(true);
            Item item = stack.getItem();
            this.writeVarInt(Item.getRawId(item));
            this.writeVarInt(stack.getCount());
            NbtCompound nbtCompound = null;
            if (item.isDamageable() || item.isNbtSynced()) {
                nbtCompound = stack.getNbt();
            }

            this.writeNbt(nbtCompound);
        }

        cir.setReturnValue((PacketByteBuf)(Object)this);
    }

    @Inject(method = "readItemStack", at = @At("HEAD"), cancellable = true)
    public void readItemStack(CallbackInfoReturnable<ItemStack> cir) {
        if (!this.readBoolean()) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else {
            int i = this.readVarInt();
            int count = this.readVarInt();
            final var item = Item.byRawId(i);
            ItemStack itemStack = count>64? new FortressItemStack(item, count) : new ItemStack(item, count);

            itemStack.setNbt(this.readNbt());
            cir.setReturnValue(itemStack);
        }
    }

}
