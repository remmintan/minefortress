package org.minefortress.fortress.automation.areas;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector4f;
import org.minefortress.network.c2s.C2SAddAreaPacket;
import org.minefortress.network.c2s.C2SRemoveAutomationAreaPacket;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.selections.renderer.ISelectionInfoProvider;
import org.minefortress.selections.renderer.ISelectionModelBuilderInfoProvider;
import org.minefortress.utils.BuildingHelper;
import org.minefortress.utils.ModUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class AreasClientManager implements ISelectionInfoProvider, ISelectionModelBuilderInfoProvider {

    private final SavedAreasHolder savedAreasHolder = new SavedAreasHolder();
    private boolean needsUpdate;
    private ProfessionsSelectionType selectionType;
    private BlockPos selectionStart;
    private BlockPos selectionEnd;
    private AutomationAreaInfo hoveredArea;
    private boolean isCorrectState = true;

    public boolean select(HitResult target) {
        if(target == null) return false;
        if(target instanceof BlockHitResult bhr) {
            if(selectionType == null) {
                MinecraftClient.getInstance()
                        .inGameHud
                        .getChatHud()
                        .addMessage(Text.literal("Please select an area type first!"));
                return false;
            }
            final var blockPos = bhr.getBlockPos();
            if(selectionStart == null) {
                this.needsUpdate = true;
                selectionStart = blockPos.toImmutable();
                selectionEnd = blockPos.toImmutable();
            } else {
                if(isCorrectState) {
                    final var selectedBlocks = Collections.unmodifiableList(getSelectedBlocks());
                    final var info = new AutomationAreaInfo(
                            selectedBlocks,
                            selectionType,
                            UUID.randomUUID()
                    );
                    final var packet = new C2SAddAreaPacket(info);
                    FortressClientNetworkHelper.send(C2SAddAreaPacket.CHANNEL, packet);
                } else {
                    MinecraftClient.getInstance()
                            .inGameHud
                            .getChatHud()
                            .addMessage(Text.literal("The selected area is not inside the fortress!"));
                }
                resetSelection();
            }
        }
        return true;
    }

    public void updateSelection(HitResult crosshairTarget) {
        if(crosshairTarget instanceof BlockHitResult bhr) {
            final var blockPos = bhr.getBlockPos();
            if(blockPos == null) return;
            this.hoveredArea = getSavedAreasHolder().getHovered(blockPos).orElse(null);

            final var possibleEnd = blockPos.toImmutable();
            if(selectionStart != null) {
                if(possibleEnd != null && !possibleEnd.equals(selectionEnd)) {
                    selectionEnd = possibleEnd;
                    needsUpdate = true;
                }
            }

            isCorrectState = ModUtils.getInfluenceManager()
                    .getFortressBorder()
                    .map(it -> getSelectedBlocks().stream().allMatch(it::contains))
                    .orElse(true);

        }
    }

    public void resetSelection() {
        this.selectionEnd = null;
        this.selectionStart = null;
        this.needsUpdate = true;
        this.hoveredArea = null;
        this.isCorrectState = true;
    }

    public void removeHovered() {
        if(hoveredArea != null) {
            final var packet = new C2SRemoveAutomationAreaPacket(hoveredArea.getId());
            FortressClientNetworkHelper.send(C2SRemoveAutomationAreaPacket.CHANNEL, packet);
        }
    }

    public boolean isSelecting() {
        return selectionStart != null;
    }

    @Override
    public boolean isNeedsUpdate() {
        return this.needsUpdate;
    }

    @Override
    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    @Override
    public List<BlockPos> getSelectedBlocks() {
        if(selectionStart == null || selectionEnd == null) return List.of();
        final var world = MinecraftClient.getInstance().world;
        if(world == null) return List.of();
        return Streams
                .stream(BlockPos.iterate(selectionStart.withY(-64), selectionEnd.withY(320)))
                .map(BlockPos::toImmutable)
                .filter(it -> BuildingHelper.canRemoveBlock(world, it) && BuildingHelper.canPlaceBlock(world, it.up()))
                .toList();
    }

    public ProfessionsSelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(ProfessionsSelectionType selectionType) {
        this.selectionType = selectionType;
    }

    @Override
    public Vector4f getClickColor() {
        if(!isCorrectState) {
            return new Vector4f((170f/255f), 0.0f, 0.0f, 0.5f);
        }
        return selectionType.getColor();
    }
    @Override
    public List<Pair<Vec3i, Vec3i>> getSelectionDimensions() {
        return Collections.emptyList();
    }

    public SavedAreasHolder getSavedAreasHolder() {
        return savedAreasHolder;
    }

    public Optional<String> getHoveredAreaName() {
        return Optional.ofNullable(hoveredArea)
                .map(AutomationAreaInfo::getAreaType)
                .map(ProfessionsSelectionType::getTitle);
    }

}
