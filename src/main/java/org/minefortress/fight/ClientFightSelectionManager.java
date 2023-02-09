package org.minefortress.fight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.interfaces.ITargetedPawn;
import org.minefortress.registries.FortressEntities;
import org.minefortress.utils.ModUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientFightSelectionManager {


    private MousePos selectionStartPos;
    private Vec3d selectionStartBlock;
    private MousePos selectionCurPos;
    private Vec3d selectionCurBlock;

    private final List<ITargetedPawn> selectedPawns = new ArrayList<>();

    private Vec3d cachedBlockPos;

    public void startSelection(double x, double y, Vec3d startBlock) {
        this.resetSelection();
        this.selectionStartPos = new MousePos(x, y);
        this.selectionStartBlock = startBlock;
    }

    public void endSelection() {
        this.selectionStartBlock = null;
        this.selectionStartPos = null;
        this.selectionCurBlock = null;
        this.selectionCurPos = null;
        if(!hasSelected()) {
            final var message = Text.of("Only warriors and archers can be selected and controlled directly.");
            MinecraftClient
                    .getInstance()
                    .inGameHud
                    .getChatHud()
                    .addMessage(message);
        }
    }

    public boolean hasSelected() {
        return !this.selectedPawns.isEmpty();
    }

    public void updateSelection(Mouse mouse, BlockHitResult target) {
        Vec3d pos = target.getPos();
        this.updateSelection(mouse.getX(), mouse.getY(), pos);
    }

    public void updateSelection(double x, double y, @Nullable Vec3d endBlock) {
        if(endBlock == null) return;
        if(!isSelectionStarted()) return;
        this.selectionCurPos = new MousePos(x, y);
        this.selectionCurBlock = endBlock;

        if(!this.selectionCurBlock.equals(this.cachedBlockPos)) {
            selectedPawns.clear();
            selectPawnsByType(FortressEntities.WARRIOR_PAWN_ENTITY_TYPE);
            selectPawnsByType(FortressEntities.ARCHER_PAWN_ENTITY_TYPE);

            this.cachedBlockPos = selectionCurBlock;
        }
    }

    private void selectPawnsByType(EntityType<? extends ITargetedPawn> type) {
        final List<ITargetedPawn> selectedPawns1;
        final var selectionBox = new Box(selectionStartBlock.getX(), -64, selectionStartBlock.getZ(), selectionCurBlock.getX(), 256, selectionCurBlock.getZ());
        final var world = MinecraftClient.getInstance().world;
        if(world != null) {
            final var playerId = ModUtils.getCurrentPlayerUUID();
            selectedPawns1 = world
                    .getEntitiesByType(type, selectionBox, it -> it.getMasterId().map(playerId::equals).orElse(false))
                    .stream()
                    .map(ITargetedPawn.class::cast)
                    .toList();
            selectedPawns.addAll(selectedPawns1);
        }
    }

    public void resetSelection() {
        this.selectionStartPos = null;
        this.selectionStartBlock = null;
        this.selectionCurPos = null;
        this.selectionCurBlock = null;
        this.selectedPawns.clear();
    }

    public boolean isSelecting() {
        return this.selectionStartPos != null && this.selectionStartBlock != null && this.selectionCurPos != null && this.selectionCurBlock != null;
    }

    public boolean isSelectionStarted() {
        return this.selectionStartPos != null && this.selectionStartBlock != null;
    }

    public void forEachSelected(Consumer<ITargetedPawn> action) {
        selectedPawns.forEach(action);
    }

    public MousePos getSelectionStartPos() {
        return selectionStartPos;
    }

    public MousePos getSelectionCurPos() {
        return selectionCurPos;
    }

    public boolean isSelected(ITargetedPawn colonist) {
        return this.selectedPawns.contains(colonist);
    }

    public record MousePos(double x, double y) {
        public int getX() {
            return (int) x;
        }

        public int getY() {
            return (int) y;
        }
    }


}
