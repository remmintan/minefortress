package org.minefortress.fight.influence;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.data.StrctureBlockData;
import org.minefortress.blueprints.interfaces.IBlockDataProvider;
import org.minefortress.blueprints.manager.BaseClientStructureManager;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.network.c2s.C2SCaptureInfluencePositionPacket;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.professions.hire.ProfessionsHireTypes;
import org.minefortress.utils.ModUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClientInfluenceManager extends BaseClientStructureManager {

    private static final BlueprintMetadata INFLUENCE_FLAG_METADATA = new BlueprintMetadata("Influence Flag", "influence_flag", 0, null);

    private final InfluenceFlagBlockDataProvider blockDataProvider = new InfluenceFlagBlockDataProvider();
    private final FortressBorderHolder fortressBorderHolder = new FortressBorderHolder(this);

    private boolean isSelectingInfluencePosition = false;

    public ClientInfluenceManager(MinecraftClient client) {
        super(client);
    }

    public Optional<WorldBorder> getFortressBorder() {
        return fortressBorderHolder.getFortressBorder();
    }

    public void startSelectingInfluencePosition() {
        isSelectingInfluencePosition = true;
    }

    public void cancelSelectingInfluencePosition() {
        super.reset();
        isSelectingInfluencePosition = false;
    }

    public void sync(List<BlockPos> positions) {
        fortressBorderHolder.syncInfluencePositions(positions);
    }

    public void selectInfluencePosition() {
        isSelectingInfluencePosition = false;
        final var pos = getStructureBuildPos();
        if(pos != null) {
            final var blockData = blockDataProvider.getBlockData(INFLUENCE_FLAG_METADATA.getFile(), BlockRotation.NONE);
            final var fortressClientManager = ModUtils.getFortressClientManager();
            final boolean notEnoughResources = isNotEnoughResources(blockData, fortressClientManager);
            if(notEnoughResources) {
                final var message = Text.of("You don't have the required items to capture this influence point!");
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
    }

    private static boolean isNotEnoughResources(StrctureBlockData blockData, FortressClientManager fortressClientManager) {
        final var resourceManager = fortressClientManager.getResourceManager();
        return !resourceManager.hasItems(blockData.getStacks());
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
                .ifPresent(it -> it.addTask(taskId, blockData.getLayer(BlueprintDataLayer.GENERAL).keySet()));

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
}
