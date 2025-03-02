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
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.renderer.CameraTools;
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
        final IFortressMinecraftClient fortressClient = (IFortressMinecraftClient) this.client;
        if (!FortressGamemodeUtilsKt.isClientInFortressGamemode() || this.client.options.pickItemKey.isPressed()) {
            return super.raycast(maxDistance, tickDelta, includeFluids);
        }

        if(fortressClient.get_FortressHud().isHovered()) {
            return BlockHitResult.createMissed(new Vec3d(0, 0, 0), null, null);
        }

        Vec3d vec3 = this.getCameraPosVec(tickDelta);
        final var mouse = this.client.mouse;
        // need to send the same mouse credentials that I can obtain from the client because
        // we need to send different mouse coordinates inside FightSelectionManager
        Vec3d vec31 = CameraTools.getMouseBasedViewVector(this.client, mouse.getX(), mouse.getY());
        Vec3d vec32 = vec3.add(vec31.x * maxDistance, vec31.y * maxDistance, vec31.z * maxDistance);
        return this.getWorld().raycast(new RaycastContext(vec3, vec32, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, this));
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (FortressGamemodeUtilsKt.isClientInFortressGamemode()) {
            if(client.options.sprintKey.isPressed()) {
                final var fortressClient = ClientModUtils.getManagersProvider();
                final IClientBlueprintManager clientBlueprintManager = fortressClient.get_BlueprintManager();
                if(clientBlueprintManager.isSelecting()) {
                    clientBlueprintManager.rotateSelectedStructureCounterClockwise();
                } else {
                    final var selectionManager = fortressClient.get_SelectionManager();
                    selectionManager.moveSelectionDown();
                }
            }
            cir.setReturnValue(false);
        }
    }

}
