package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.block.Block;
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
import org.slf4j.LoggerFactory;

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
    public static Optional<IServerManagersProvider> getManagersProvider(IFortressAwareEntity entity) {
        return getManagersProvider(entity.getServer(), entity.getFortressPos());
    }

    @NotNull
    public static Optional<IServerFortressManager> getFortressManager(IFortressAwareEntity entity) {
        return getFortressManager(entity.getServer(), entity.getFortressPos());
    }

    @NotNull
    public static Optional<IServerManagersProvider> getManagersProvider(MinecraftServer server, BlockPos fortressCenter) {
        return getFortressHolder(server, fortressCenter).map(IFortressHolder::getServerManagersProvider);
    }

    @NotNull
    public static Optional<IServerFortressManager> getFortressManager(MinecraftServer server, BlockPos fortressCenter) {
        return getFortressHolder(server, fortressCenter).map(IFortressHolder::getServerFortressManager);
    }

    public static void addDropToTheResourceManager(ServerWorld w, BlockPos pos, IFortressAwareEntity c) {
        if (ServerExtensionsKt.isSurvivalFortress(c.getServer())) {
            getManagersProvider(c).ifPresent(it -> {
                final var blockState = w.getBlockState(pos);
                final var blockEntity = w.getBlockEntity(pos);
                // FIXME: consider the tool and the entity
                final var drop = Block.getDroppedStacks(blockState, w, pos, blockEntity);
                if (!it.getResourceHelper().putItemsToSuitableContainer(drop)) {
                    // FIXME: chests are full!
                    LoggerFactory.getLogger(ServerModUtils.class).error("THE ITEMS ARE NOT SAVED, ALL CHESTS FULL");
                }
            });
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
