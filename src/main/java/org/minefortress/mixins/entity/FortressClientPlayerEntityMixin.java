package org.minefortress.mixins.entity;

import com.chocohead.mm.api.ClassTinkerers;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.CameraTools;
import org.minefortress.selections.SelectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class FortressClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow public abstract float getYaw(float tickDelta);

    @Shadow public abstract float getPitch(float tickDelta);

    @Shadow public Input input;

    @Shadow protected int ticksLeftToDoubleTapSprint;

    @Shadow protected abstract boolean isWalking();

    public FortressClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public HitResult raycast(double maxDistance, float tickDelta, boolean includeFluids) {
        if(((FortressMinecraftClient)client).isNotFortressGamemode() || client.options.keyPickItem.isPressed()){
            return super.raycast(maxDistance, tickDelta, includeFluids);
        }

        Vec3d vec3 = this.getCameraPosVec(tickDelta);
        Vec3d vec31 = CameraTools.getMouseBasedViewVector(this.client, this.getPitch(), this.getYaw());
        Vec3d vec32 = vec3.add(vec31.x * maxDistance, vec31.y * maxDistance, vec31.z * maxDistance);
        return this.world.raycast(new RaycastContext(vec3, vec32, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, this));
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if(client.interactionManager != null && client.interactionManager.getCurrentGameMode() == ClassTinkerers.getEnum(GameMode.class, "FORTRESS")) {
            if(client.options.keySprint.isPressed()) {
                final SelectionManager selectionManager = ((FortressMinecraftClient) client).getSelectionManager();
                selectionManager.moveSelectionDown();
                cir.setReturnValue(false);
            }
        }
    }

//    @Redirect(method = "tickMovement", at = @At(value = "HEAD"))
//    public void tickMovement(CallbackInfo ci) {
//        boolean bl2 = this.input.sneaking;
//        boolean bl3 = this.isWalking();
//
//        boolean bl5 = (float)this.getHungerManager().getFoodLevel() > 6.0f || this.getAbilities().allowFlying;
//        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
//        if (!(!fortressClient.isFortressGamemode() && !this.isSubmergedInWater() || bl2 || bl3 || !this.isWalking() || this.isSprinting() || !bl5 || this.isUsingItem() || this.hasStatusEffect(StatusEffects.BLINDNESS))) {
//            if (this.ticksLeftToDoubleTapSprint > 0 || this.client.options.keySprint.isPressed()) {
//                this.setSprinting(true);
//            } else {
//                this.ticksLeftToDoubleTapSprint = 7;
//            }
//        }
//
//    }

}
