package org.minefortress.mixins.entity;

import com.chocohead.mm.api.ClassTinkerers;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import org.minefortress.CameraTools;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.selections.SelectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class FortressClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow public abstract float getYaw(float tickDelta);

    @Shadow public abstract float getPitch(float tickDelta);

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

}
