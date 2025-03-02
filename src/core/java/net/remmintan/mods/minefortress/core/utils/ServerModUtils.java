package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressHolder;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ServerModUtils {

    public static boolean hasFortress(@NotNull ServerPlayerEntity player) {
        final var fortressPos = ((IFortressPlayerEntity) player).get_FortressPos();
        return fortressPos.isPresent();
    }

    @NotNull
    public static IServerManagersProvider getManagersProvider(ServerPlayerEntity player) {
        final var fortressPos = ((IFortressPlayerEntity) player).get_FortressPos().orElseThrow();
        final var fortressHolder = getFortressHolder(player.getServer(), fortressPos);
        return fortressHolder.getServerManagersProvider();
    }

    @NotNull
    public static IServerFortressManager getFortressManager(ServerPlayerEntity player) {
        final var fortressPos = ((IFortressPlayerEntity) player).get_FortressPos().orElseThrow();
        final var fortressHolder = getFortressHolder(player.getServer(), fortressPos);
        return fortressHolder.getServerFortressManager();
    }

    public static IServerFortressManager getFortressManager(MinecraftServer server, BlockPos fortressCenter) {
        final var fortressHolder = getFortressHolder(server, fortressCenter);
        return fortressHolder.getServerFortressManager();
    }

    @NotNull
    public static IServerManagersProvider getManagersProvider(IFortressAwareEntity entity) {
        final var fortressPos = entity.getFortressPos();
        final var fortressHolder = getFortressHolder(entity.getServer(), fortressPos);
        return fortressHolder.getServerManagersProvider();
    }

    @NotNull
    public static IServerFortressManager getFortressManager(IFortressAwareEntity entity) {
        final var fortressPos = entity.getFortressPos();
        final var fortressHolder = getFortressHolder(entity.getServer(), fortressPos);
        return fortressHolder.getServerFortressManager();
    }

    @NotNull
    public static IServerManagersProvider getManagersProvider(MinecraftServer server, BlockPos fortressCenter) {
        return getFortressHolder(server, fortressCenter).getServerManagersProvider();
    }

    @NotNull
    private static IFortressHolder getFortressHolder(@Nullable MinecraftServer server, BlockPos p) {
        if (server == null) throw new IllegalArgumentException("Server cannot be null");
        final var blockEntity = server.getOverworld().getBlockEntity(p);
        if (blockEntity instanceof IFortressHolder holder) {
            return holder;
        }
        throw new IllegalStateException("Could not find fortress at " + p);
    }

    public static void addDropToTheResourceManager(ServerWorld w, BlockPos g, IFortressAwareEntity c) {
        final var blockState = w.getBlockState(g);
        final var blockEntity = blockState instanceof BlockEntityProvider provider ? provider.createBlockEntity(g, blockState) : null;
        final var drop = Block.getDroppedStacks(blockState, w, g, blockEntity);

        if (ServerExtensionsKt.isSurvivalFortress(c.getServer())) {
            final var provider = getManagersProvider(c);
            final var serverResourceManager = provider.getResourceManager();
            for (ItemStack itemStack : drop) {
                final var item = itemStack.getItem();
                final var count = itemStack.getCount();
                serverResourceManager.increaseItemAmount(item, count);
            }
        }
    }
}
