package org.minefortress.mixins.renderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.remmintan.panama.view.SelectionBlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(FluidRenderer.class)
public abstract class FortressFluidRendererMixin {


    private final AtomicBoolean fortressFluid = new AtomicBoolean(false);
    private volatile BlockPos fortressFluidPos = null;

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void renderHead(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if(world instanceof SelectionBlockRenderView) {
            this.fortressFluid.set(true);
            this.fortressFluidPos = pos;
        }
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    public void renderReturn(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        this.fortressFluid.set(false);
        this.fortressFluidPos = null;
    }

    @Inject(method="vertex", at = @At(value = "HEAD"), cancellable = true)
    void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light, CallbackInfo ci) {
        if(this.fortressFluid.get()) {
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
