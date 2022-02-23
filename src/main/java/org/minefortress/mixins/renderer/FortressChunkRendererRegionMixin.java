package org.minefortress.mixins.renderer;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.RenderedChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.selections.ClickType;
import org.minefortress.selections.SelectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(RenderedChunk.class)
public abstract class FortressChunkRendererRegionMixin {

    private final GameMode FORTRESS_MODE = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    public void getBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if( isNotFortressGameMode()) return;

        final FortressMinecraftClient fortressClient = getFortressClient();
        final SelectionManager selectionManager = fortressClient.getSelectionManager();
        final FortressClientManager fortressManager = fortressClient.getFortressClientManager();

        if(fortressManager.isFortressInitializationNeeded()) {
            final BlockPos posAppropriateForCenter = fortressManager.getPosAppropriateForCenter();
            if(pos.equals(posAppropriateForCenter)) {
                cir.setReturnValue(fortressManager.getStateForCampCenter());
            }
            return;
        }

        if(selectionManager.getClickType() == ClickType.BUILD) {
            final Set<BlockPos> selectedBlocks = selectionManager.getSelectedBlocks();
            final BlockState state = selectionManager.getClickingBlock();

            if (selectedBlocks.contains(pos)) {
                cir.setReturnValue(state);
            }
        }
    }

    private boolean isNotFortressGameMode() {
        final MinecraftClient client = MinecraftClient.getInstance();
        return client.interactionManager == null || client.interactionManager.getCurrentGameMode() != FORTRESS_MODE;
    }

    private FortressMinecraftClient getFortressClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
    }

}
