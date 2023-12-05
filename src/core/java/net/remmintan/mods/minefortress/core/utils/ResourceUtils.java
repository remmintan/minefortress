package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;

public class ResourceUtils {

    public static void addDropToTheResourceManager(ServerWorld w, BlockPos g, IFortressAwareEntity c) {
        final var blockState = w.getBlockState(g);
        final var blockEntity = blockState instanceof BlockEntityProvider provider ? provider.createBlockEntity(g, blockState) : null;
        final var drop = Block.getDroppedStacks(blockState, w, g, blockEntity);

        final var provider = c.getManagersProvider().orElseThrow();
        final var manager = c.getServerFortressManager().orElseThrow();
        if(manager.isSurvival()) {
            final var serverResourceManager = provider.getResourceManager();
            for (ItemStack itemStack : drop) {
                final var item = itemStack.getItem();
                final var count = itemStack.getCount();
                serverResourceManager.increaseItemAmount(item, count);
            }
        }
    }

}
