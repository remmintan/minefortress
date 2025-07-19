package org.minefortress.mixins.block.entity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.events.InventoryDirtyCallback;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class FortressBlockEntity {

    @Shadow
    @Nullable
    protected World world;

    @Shadow
    @Final
    protected BlockPos pos;

    @Inject(method = "markDirty()V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/entity/BlockEntity;markDirty(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
    public void markDirty(CallbackInfo ci) {
        if (this.world instanceof ServerWorld sw && pos != null) {
            InventoryDirtyCallback.Companion.getEVENT().invoker().onDirty(sw, pos);
        }
    }


}
