package org.minefortress.fight.influence;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderStage;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlockDataProvider;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.infuence.IClientInfluenceManager;
import net.remmintan.mods.minefortress.core.interfaces.infuence.IInfluencePosStateHolder;
import net.remmintan.mods.minefortress.core.interfaces.professions.ProfessionsHireTypes;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.C2SCaptureInfluencePositionPacket;
import net.remmintan.mods.minefortress.networking.c2s.C2SUpdateNewInfluencePosition;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.blueprints.manager.BaseClientStructureManager;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.utils.ModUtils;

import java.util.*;

public class ClientInfluenceManager extends BaseClientStructureManager implements IClientInfluenceManager {

    private static final IBlueprintMetadata INFLUENCE_FLAG_METADATA = new BlueprintMetadata("Influence Flag", "influence_flag", 0, 0);

    private final InfluenceFlagBlockDataProvider blockDataProvider = new InfluenceFlagBlockDataProvider();
    private final ClientFortressBorderHolder clientFortressBorderHolder = new ClientFortressBorderHolder(this);
    private final IInfluencePosStateHolder influencePosStateHolder = new InfluencePosStateHolder();

    private boolean isSelectingInfluencePosition = false;

    public ClientInfluenceManager(MinecraftClient client) {
        super(client);
    }

    public static boolean influenceEnabled() {
        final var fortressClientManager = ModUtils.getFortressClientManager();
        return fortressClientManager.isSurvival() && fortressClientManager.isBorderEnabled();
    }

    @Override
    public void tick() {
        super.tick();
        influencePosStateHolder.syncNewPos(getStructureBuildPos());
    }

    @Override
    public Optional<WorldBorder> getFortressBorder() {
        if(influenceEnabled()) {
            return clientFortressBorderHolder.getFortressBorder();
        } else {
            return Optional.ofNullable(MinecraftClient.getInstance().world)
                    .map(ClientWorld::getWorldBorder);
        }
    }

    @Override
    public void startSelectingInfluencePosition() {
        isSelectingInfluencePosition = true;
    }

    @Override
    public void cancelSelectingInfluencePosition() {
        super.reset();
        isSelectingInfluencePosition = false;
        influencePosStateHolder.reset();
    }

    @Override
    public void sync(List<BlockPos> positions) {
        clientFortressBorderHolder.syncInfluencePositions(positions);
    }

    @Override
    public void selectInfluencePosition() {
        isSelectingInfluencePosition = false;
        final var pos = getStructureBuildPos();
        if(pos != null) {
            final var blockData = blockDataProvider.getBlockData(INFLUENCE_FLAG_METADATA.getId(), BlockRotation.NONE);
            final var fortressClientManager = ModUtils.getFortressClientManager();

            String stageMessage = switch (influencePosStateHolder.getWorldBorderStage()) {
                case STATIONARY -> "This influence point is already captured!";
                case SHRINKING -> "This influence point is too farm away from your fortress!";
                default -> null;
            };

            if(stageMessage != null) {
                final var message = Text.of(stageMessage);
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
                super.reset();
                return;
            }

            if(!isEnoughResources()) {
                final var msg = "You don't have the required items to capture this influence point!";
                final var message = Text.of(msg);
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
                super.reset();
                return;
            }

            boolean hasAnyWarrior = isHasAnyWarrior(fortressClientManager);
            if(!hasAnyWarrior) {
                final var message = Text.of("You don't have any warriors to  capture this influence point!");
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
                super.reset();
                return;
            }

            sendCaptureTaskPacket(pos, blockData);
        }
        influencePosStateHolder.reset();
    }

    private static boolean isHasAnyWarrior(IClientFortressManager clientFortressManager) {
        final var professionManager = clientFortressManager.getProfessionManager();
        final var warriorIds = ProfessionsHireTypes.WARRIORS.getIds();
        var hasAnyWarrior = false;
        for (String warriorId : warriorIds) {
            if(hasAnyWarrior) break;
            hasAnyWarrior = professionManager.hasProfession(warriorId);
        }
        return hasAnyWarrior;
    }

    @Override
    public void sendCaptureTaskPacket(BlockPos pos, IStructureBlockData blockData) {
        final var taskId = UUID.randomUUID();
        CoreModUtils.getClientTasksHolder()
                .ifPresent(it -> {
                    final var blocks = blockData.getLayer(BlueprintDataLayer.GENERAL)
                            .entrySet()
                            .stream()
                            .filter(e -> e.getValue() != null && !e.getValue().isAir())
                            .map(Map.Entry::getKey)
                            .filter(Objects::nonNull)
                            .map(BlockPos::toImmutable)
                            .map(p -> p.add(pos))
                            .toList();
                    it.addTask(taskId, blocks, TaskType.BUILD, null);
                });

        final var packet = new C2SCaptureInfluencePositionPacket(taskId, pos);
        FortressClientNetworkHelper.send(C2SCaptureInfluencePositionPacket.CHANNEL, packet);
        reset();
    }

    @Override
    public IBlockDataProvider getBlockDataProvider() {
        return blockDataProvider;
    }

    @Override
    public boolean isSelecting() {
        return isSelectingInfluencePosition;
    }

    @Override
    public IBlueprintMetadata getSelectedStructure() {
        return INFLUENCE_FLAG_METADATA;
    }

    @Override
    public boolean canBuild() {
        return getInfluencePosStateHolder().getWorldBorderStage() != WorldBorderStage.SHRINKING
                && super.canBuild();
    }

    @Override
    public IInfluencePosStateHolder getInfluencePosStateHolder() {
        return influencePosStateHolder;
    }

    public static class InfluencePosStateHolder implements IInfluencePosStateHolder {
        private WorldBorderStage worldBorderStage = WorldBorderStage.STATIONARY;
        private BlockPos lastPos = null;

        @Override
        public void syncNewPos(BlockPos newPos) {
            if(newPos == null) {
                setCorrect(WorldBorderStage.STATIONARY);
                lastPos = null;
                return;
            }
            final var alignedPos = BaseFortressBorderHolder.alignToAGrid(newPos);
            if(Objects.equals(lastPos, alignedPos)) {
                return;
            }

            final var packet = new C2SUpdateNewInfluencePosition(alignedPos);
            FortressClientNetworkHelper.send(C2SUpdateNewInfluencePosition.CHANNEL, packet);
            lastPos = alignedPos;
        }

        @Override
        public void setCorrect(WorldBorderStage state) {
            worldBorderStage = state;
        }

        @Override
        public WorldBorderStage getWorldBorderStage() {
            return worldBorderStage;
        }

        @Override
        public void reset() {
            worldBorderStage = WorldBorderStage.STATIONARY;
            lastPos = null;
        }

    }
}
