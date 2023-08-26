package org.minefortress.mixins.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.CameraTools;
import org.minefortress.selections.SelectionManager;
import org.minefortress.utils.ModUtils;
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
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        if(!fortressClient.is_FortressGamemode() || this.client.options.pickItemKey.isPressed()){
            return super.raycast(maxDistance, tickDelta, includeFluids);
        }

        if(fortressClient.get_FortressHud().isHovered()) {
            return BlockHitResult.createMissed(new Vec3d(0, 0, 0), null, null);
        }

        Vec3d vec3 = this.getCameraPosVec(tickDelta);
        Vec3d vec31 = CameraTools.getMouseBasedViewVector(this.client, this.getPitch(), this.getYaw());
        Vec3d vec32 = vec3.add(vec31.x * maxDistance, vec31.y * maxDistance, vec31.z * maxDistance);
        return this.getWorld().raycast(new RaycastContext(vec3, vec32, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, this));
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if(ModUtils.isClientInFortressGamemode()) {
            if(client.options.sprintKey.isPressed()) {
                final FortressMinecraftClient fortressClient = ModUtils.getFortressClient();
                final ClientBlueprintManager clientBlueprintManager = fortressClient.get_BlueprintManager();
                if(clientBlueprintManager.isSelecting()) {
                    clientBlueprintManager.rotateSelectedStructureCounterClockwise();
                } else {
                    final SelectionManager selectionManager = fortressClient.get_SelectionManager();
                    selectionManager.moveSelectionDown();
                }
            }
            cir.setReturnValue(false);
        }
    }

}
