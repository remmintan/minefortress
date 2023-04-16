package org.minefortress.fight.influence;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderStage;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.data.StrctureBlockData;
import org.minefortress.blueprints.interfaces.IBlockDataProvider;
import org.minefortress.blueprints.manager.BaseClientStructureManager;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.network.c2s.C2SCaptureInfluencePositionPacket;
import org.minefortress.network.c2s.C2SUpdateNewInfluencePosition;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.professions.hire.ProfessionsHireTypes;
import org.minefortress.utils.ModUtils;

import java.util.*;

public class ClientInfluenceManager extends BaseClientStructureManager {

    private static final BlueprintMetadata INFLUENCE_FLAG_METADATA = new BlueprintMetadata("Influence Flag", "influence_flag", 0, null);

    private final InfluenceFlagBlockDataProvider blockDataProvider = new InfluenceFlagBlockDataProvider();
    private final ClientFortressBorderHolder clientFortressBorderHolder = new ClientFortressBorderHolder(this);
    private final InfluencePosStateHolder influencePosStateHolder = new InfluencePosStateHolder();

    private boolean isSelectingInfluencePosition = false;

    public ClientInfluenceManager(MinecraftClient client) {
        super(client);
    }

    @Override
    public void tick() {
        super.tick();
        influencePosStateHolder.syncNewPos(getStructureBuildPos());
    }

    public Optional<WorldBorder> getFortressBorder() {
        return clientFortressBorderHolder.getFortressBorder();
    }

    public void startSelectingInfluencePosition() {
        isSelectingInfluencePosition = true;
    }

    public void cancelSelectingInfluencePosition() {
        super.reset();
        isSelectingInfluencePosition = false;
        influencePosStateHolder.reset();
    }

    public void sync(List<BlockPos> positions) {
        clientFortressBorderHolder.syncInfluencePositions(positions);
    }

    public void selectInfluencePosition() {
        isSelectingInfluencePosition = false;
        final var pos = getStructureBuildPos();
        if(pos != null) {
            final var blockData = blockDataProvider.getBlockData(INFLUENCE_FLAG_METADATA.getFile(), BlockRotation.NONE);
            final var fortressClientManager = ModUtils.getFortressClientManager();

            String stageMessage = switch (influencePosStateHolder.getWorldBorderStage()) {
                case STATIONARY -> "This influence point is already captured!";
                case SHRINKING -> "This influence point is too close to the enemy fortress!";
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

    private static boolean isHasAnyWarrior(FortressClientManager fortressClientManager) {
        final var professionManager = fortressClientManager.getProfessionManager();
        final var warriorIds = ProfessionsHireTypes.WARRIORS.getIds();
        var hasAnyWarrior = false;
        for (String warriorId : warriorIds) {
            if(hasAnyWarrior) break;
            hasAnyWarrior = professionManager.hasProfession(warriorId);
        }
        return hasAnyWarrior;
    }

    private void sendCaptureTaskPacket(BlockPos pos, StrctureBlockData blockData) {
        final var taskId = UUID.randomUUID();
        ModUtils.getClientTasksHolder()
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
                    it.addTask(taskId, blocks);
                });

        final var packet = new C2SCaptureInfluencePositionPacket(taskId, pos);
        FortressClientNetworkHelper.send(C2SCaptureInfluencePositionPacket.CHANNEL, packet);
        super.reset();
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
    public BlueprintMetadata getSelectedStructure() {
        return INFLUENCE_FLAG_METADATA;
    }

    @Override
    public boolean canBuild() {
        return getInfluencePosStateHolder().getWorldBorderStage() != WorldBorderStage.SHRINKING
                && super.canBuild();
    }

    public InfluencePosStateHolder getInfluencePosStateHolder() {
        return influencePosStateHolder;
    }

    public static class InfluencePosStateHolder {
        private WorldBorderStage worldBorderStage = WorldBorderStage.STATIONARY;
        private BlockPos lastPos = null;

        void syncNewPos(BlockPos newPos) {
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

        public void setCorrect(WorldBorderStage state) {
            worldBorderStage = state;
        }

        public WorldBorderStage getWorldBorderStage() {
            return worldBorderStage;
        }

        void reset() {
            worldBorderStage = WorldBorderStage.STATIONARY;
            lastPos = null;
        }

    }
}
