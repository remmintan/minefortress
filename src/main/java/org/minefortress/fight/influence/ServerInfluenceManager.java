package org.minefortress.fight.influence;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorderStage;
import net.remmintan.mods.minefortress.core.interfaces.infuence.ICaptureTask;
import net.remmintan.mods.minefortress.core.interfaces.infuence.IServerInfluenceManager;
import org.jetbrains.annotations.Nullable;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlockDataProvider;
import org.minefortress.fortress.ServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket;
import net.remmintan.mods.minefortress.networking.s2c.S2CSyncInfluence;
import net.remmintan.mods.minefortress.networking.s2c.S2CUpdateInfluenceBorderStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ServerInfluenceManager implements IServerInfluenceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerInfluenceManager.class);
    private final Deque<ICaptureTask> captureTasksQueue = new ConcurrentLinkedDeque<>();
    private final List<BlockPos> allInfluencePositions = new ArrayList<>();
    private final Synchronizer synchronizer = new Synchronizer();
    private final InfluenceFlagBlockDataProvider influenceFlagBlockDataProvider = new InfluenceFlagBlockDataProvider();
    private final ServerFortressBorderHolder fortressBorderHolder = new ServerFortressBorderHolder();

    private final ServerFortressManager serverFortressManager;

    public ServerInfluenceManager(ServerFortressManager serverFortressManager) {
        this.serverFortressManager = serverFortressManager;
    }

    @Override
    public void addCapturePosition(UUID taskId, BlockPos pos, ServerPlayerEntity player) {
        final var stage = this.fortressBorderHolder.getStage(pos);
        final var resourceManager = (IServerResourceManager)this.serverFortressManager.getResourceManager();
        final var influenceFlag = influenceFlagBlockDataProvider.getBlockData("influence_flag", BlockRotation.NONE);
        final var stacks = influenceFlag.getStacks();
        if(stage == WorldBorderStage.GROWING && (resourceManager.hasItems(stacks) || serverFortressManager.isCreative())) {
            if(serverFortressManager.isSurvival()) {
                resourceManager.reserveItems(taskId, stacks);
            }
            captureTasksQueue.add(new CaptureTask(taskId, pos));
            fortressBorderHolder.add(pos);
        } else {
            final var packet = new ClientboundTaskExecutedPacket(taskId);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, packet);
            LOGGER.warn("Player tried to capture influence flag without having the required items!");
        }
    }
    public ICaptureTask getCaptureTask() {
        return captureTasksQueue.poll();
    }

    public void failCaptureTask(ICaptureTask task) {
        captureTasksQueue.add(task);
    }

    public IBlockDataProvider getBlockDataProvider() {
        return influenceFlagBlockDataProvider;
    }

    public void addInfluencePosition(BlockPos pos) {
        allInfluencePositions.add(pos);
        synchronizer.scheduleSync();
    }

    public void tick(@Nullable ServerPlayerEntity player) {
        if(player == null) {
            return;
        }
        synchronizer.tick(player);
    }

    public void sync() {
        synchronizer.scheduleSync();
    }

    public void write(NbtCompound tag) {
        final var nbt = new NbtCompound();
        final var list = new NbtList();
        for (final var pos : allInfluencePositions) {
            final var posTag = new NbtCompound();
            posTag.putLong("pos", pos.asLong());
            list.add(posTag);
        }
        nbt.put("positions", list);

        tag.put("influenceManager", nbt);
    }

    @Override
    public void checkNewPositionAndUpdateClientState(BlockPos pos, ServerPlayerEntity player) {
        final var packet = new S2CUpdateInfluenceBorderStage(fortressBorderHolder.getStage(pos));
        FortressServerNetworkHelper.send(player, S2CUpdateInfluenceBorderStage.CHANNEL, packet);
    }

    public void read(NbtCompound tag) {
        if (!tag.contains("influenceManager")) {
            addCenterAsInfluencePosition();
            return;
        }
        NbtCompound nbt = tag.getCompound("influenceManager");

        allInfluencePositions.clear();
        fortressBorderHolder.clear();
        final var list = nbt.getList("positions", NbtList.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            final var posTag = list.getCompound(i);
            final var pos = BlockPos.fromLong(posTag.getLong("pos"));
            allInfluencePositions.add(pos);
            fortressBorderHolder.add(pos);
        }
        if(allInfluencePositions.isEmpty()) {
            addCenterAsInfluencePosition();
        }
        synchronizer.scheduleSync();
    }

    public void addCenterAsInfluencePosition() {
        final var fortressCenter = serverFortressManager.getFortressCenter();
        if(fortressCenter != null) {
            addInfluencePosition(fortressCenter);
            fortressBorderHolder.add(fortressCenter);
        }
    }

    private class Synchronizer {

        private boolean syncScheduled = false;

        public void scheduleSync() {
            if (syncScheduled) {
                return;
            }
            syncScheduled = true;
        }

        public void tick(ServerPlayerEntity player) {
            if (!syncScheduled) {
                return;
            }
            final var s2CSyncInfluence = new S2CSyncInfluence(allInfluencePositions);
            FortressServerNetworkHelper.send(player, S2CSyncInfluence.CHANNEL,  s2CSyncInfluence);
            syncScheduled = false;
        }
    }

    public record CaptureTask(UUID taskId, BlockPos pos) implements ICaptureTask { }

}
