package org.minefortress.mixins.renderer;

import com.chocohead.mm.api.ClassTinkerers;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.cloned.ClonedChunkSection;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.GameMode;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.selections.ClickType;
import org.minefortress.selections.SelectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(WorldSlice.class)
public abstract class FortressWorldSliceMixin {

    @Inject(method = "unpackBlockData", at = @At("TAIL"))
    public void unpackBlockData(BlockState[] states, ClonedChunkSection section, BlockBox box, CallbackInfo ci) {
//        final MinecraftClient client = MinecraftClient.getInstance();
//        if (client.interactionManager == null || client.interactionManager.getCurrentGameMode() != ClassTinkerers.getEnum(GameMode.class, "FORTRESS"))
//            return;
//        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
//
//        final ClientBlueprintManager clientBlueprintManager = fortressClient.getBlueprintManager();
//        if (!clientBlueprintManager.hasSelectedBlueprint()) {
//            addSelectionToChunk(states, section, fortressClient);
//        }
    }

//    private void addSelectionToChunk(BlockState[] states, ClonedChunkSection section, FortressMinecraftClient fortressClient) {
//        final SelectionManager selectionManager = fortressClient.getSelectionManager();
//        if(selectionManager.getClickType() == ClickType.BUILD) {
//            final Set<BlockPos> selectedBlocks = selectionManager.getSelectedBlocks();
//            final BlockState state = selectionManager.getClickingBlock();
//
//            final ChunkSectionPos sectionPos = section.getPosition();
//            BlockPos startPos = new BlockPos(sectionPos.getMinX(), sectionPos.getMinY(), sectionPos.getMinZ());
//            BlockPos endPos = new BlockPos(sectionPos.getMaxX(), sectionPos.getMaxY(), sectionPos.getMaxZ());
//
//            for (BlockPos blockPos : BlockPos.iterate(startPos, endPos)) {
//                if (selectedBlocks.contains(blockPos)) {
//                    final int index = WorldSlice.getLocalBlockIndex(blockPos.getX()&15, blockPos.getY()&15, blockPos.getZ()&15);
//                    states[index] = state;
//                }
//            }
//        }
//    }

}
