package org.minefortress.mixins.renderer;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.minefortress.selections.renderer.selection.SelectionBlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public class FortressFluidRendererMixin {

    private boolean fortressFluid = false;
    private BlockPos fortressFluidPos = null;

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void renderHead(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state, CallbackInfoReturnable<Boolean> cir) {
        if(world instanceof SelectionBlockRenderView) {
            this.fortressFluid = true;
            this.fortressFluidPos = pos;
        }
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    public void renderReturn(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state, CallbackInfoReturnable<Boolean> cir) {
        this.fortressFluid = false;
        this.fortressFluidPos = null;
    }

    @Inject(method="vertex", at = @At(value = "HEAD"), cancellable = true)
    void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light, CallbackInfo ci) {
        if(this.fortressFluid) {
            final int x1 = fortressFluidPos.getX();
            final int y1 = fortressFluidPos.getY();
            final int z1 = fortressFluidPos.getZ();

            final double newX = x - (x1 & 0xF) + x1;
            final double newY = y - (y1 & 0xF) + y1;
            final double newZ = z - (z1 & 0xF) + z1;

            vertexConsumer.vertex(newX, newY, newZ).color(red, green, blue, 1.0f).texture(u, v).light(light).normal(0.0f, 1.0f, 0.0f).next();
            ci.cancel();
        }
    }

}
