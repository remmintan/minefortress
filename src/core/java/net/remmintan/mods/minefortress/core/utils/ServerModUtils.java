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

import java.util.Optional;

public final class ServerModUtils {

    public static boolean hasFortress(@NotNull ServerPlayerEntity player) {
        final var fortressPos = ((IFortressPlayerEntity) player).get_FortressPos();
        return fortressPos.isPresent();
    }

    @NotNull
    public static Optional<IServerManagersProvider> getManagersProvider(ServerPlayerEntity player) {
        return ((IFortressPlayerEntity) player).get_FortressPos().flatMap(it -> getManagersProvider(player.getServer(), it));
    }

    @NotNull
    public static Optional<IServerFortressManager> getFortressManager(ServerPlayerEntity player) {
        return ((IFortressPlayerEntity) player).get_FortressPos().flatMap(it -> getFortressManager(player.getServer(), it));
    }

    @NotNull
    public static IServerManagersProvider getManagersProvider(IFortressAwareEntity entity) {
        return getManagersProvider(entity.getServer(), entity.getFortressPos()).orElseThrow();
    }

    @NotNull
    public static IServerFortressManager getFortressManager(IFortressAwareEntity entity) {
        return getFortressManager(entity.getServer(), entity.getFortressPos()).orElseThrow();
    }

    @NotNull
    public static Optional<IServerManagersProvider> getManagersProvider(MinecraftServer server, BlockPos fortressCenter) {
        return getFortressHolder(server, fortressCenter).map(IFortressHolder::getServerManagersProvider);
    }

    @NotNull
    public static Optional<IServerFortressManager> getFortressManager(MinecraftServer server, BlockPos fortressCenter) {
        return getFortressHolder(server, fortressCenter).map(IFortressHolder::getServerFortressManager);
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

    @NotNull
    private static Optional<IFortressHolder> getFortressHolder(@Nullable MinecraftServer server, @Nullable BlockPos p) {
        if (server == null || p == null) return Optional.empty();
        final var blockEntity = server.getOverworld().getBlockEntity(p);
        if (blockEntity instanceof IFortressHolder holder) {
            return Optional.of(holder);
        }
        return Optional.empty();
    }
}
