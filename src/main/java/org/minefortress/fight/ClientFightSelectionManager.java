package org.minefortress.fight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.dtos.combat.MousePos;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightSelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import org.minefortress.renderer.CameraTools;
import org.minefortress.utils.ModUtils;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientFightSelectionManager implements IClientFightSelectionManager {

    public static final int RAY_LENGTH = 200;
    private MousePos mouseStartPos;
    private MousePos mouseEndPos;
    private MousePos cachedMousePos;

    private final List<IFortressAwareEntity> selectedPawns = new ArrayList<>();


    @Override
    public void startSelection(double x, double y) {
        this.resetSelection();
        this.mouseStartPos = new MousePos(x, y);
    }

    @Override
    public void endSelection() {
        this.mouseStartPos = null;
        this.mouseEndPos = null;
        if(!hasSelected()) {
            final var message = Text.of("Only warriors and archers can be selected and controlled directly.");
            MinecraftClient
                    .getInstance()
                    .inGameHud
                    .getChatHud()
                    .addMessage(message);
        }
    }

    @Override
    public boolean hasSelected() {
        return !this.selectedPawns.isEmpty();
    }

    @Override
    public void updateSelection(Mouse mouse, BlockHitResult target) {
        this.updateSelection(mouse.getX(), mouse.getY());
    }

    @Override
    public void updateSelection(double x, double y) {
        if(!isSelectionStarted()) return;
        this.mouseEndPos = new MousePos(x, y);

        if(!this.mouseEndPos.equals(this.cachedMousePos)) {
            selectedPawns.clear();
            final var eyePos = MinecraftClient.getInstance().player.getEyePos();

            // create four square point from mouseStartPos to mouseEndPos and put them in a list
            List<MousePos> squarePoints = new ArrayList<>();
            squarePoints.add(mouseStartPos);
            squarePoints.add(new MousePos(mouseStartPos.getX(), mouseEndPos.getY()));
            squarePoints.add(mouseEndPos);
            squarePoints.add(new MousePos(mouseEndPos.getX(), mouseStartPos.getY()));

            var blockPositions = new ArrayList<Vec3d>();
            for (MousePos squarePoint : squarePoints) {
                final var viewVector = CameraTools.getMouseBasedViewVector(MinecraftClient.getInstance(), squarePoint.getX(), squarePoint.getY());;
                final var point = eyePos.add(viewVector.x * RAY_LENGTH, viewVector.y * RAY_LENGTH, viewVector.z * RAY_LENGTH);
                blockPositions.add(point);
            }
            blockPositions.add(eyePos);

            LoggerFactory.getLogger(ClientFightSelectionManager.class).info("blockPositions: {}", blockPositions);



            final var world = MinecraftClient.getInstance().world;

            List<Vec3d> eyePosList = new ArrayList<>();

            world
                .getEntities()
                .forEach(
                    entity -> {
                        if(entity instanceof IFortressAwareEntity) {
                            final var pos = entity.getEyePos();
                            eyePosList.add(pos);
                        }
                    }
                );






//            selectPawnsByType(FortressEntities.WARRIOR_PAWN_ENTITY_TYPE, blockPositions);
//            selectPawnsByType(FortressEntities.ARCHER_PAWN_ENTITY_TYPE, blockPositions);

            this.cachedMousePos = this.mouseEndPos;
        }
    }

    private void selectPawnsByType(EntityType<? extends ITargetedPawn> type, List<BlockPos> blockPositions) {
        final List<ITargetedPawn> selectedPawns1;

//        final var selectionBox = new Box(selectionStartBlock.getX(), -64, selectionStartBlock.getZ(), selectionCurBlock.getX(), 256, selectionCurBlock.getZ());
        final var world = MinecraftClient.getInstance().world;
        if(world != null) {
            final var playerId = ModUtils.getCurrentPlayerUUID();
            selectedPawns1 = world
                    .getEntitiesByType(type, null, it -> it.getMasterId().map(playerId::equals).orElse(false))
                    .stream()
                    .map(ITargetedPawn.class::cast)
                    .toList();
            selectedPawns.addAll(selectedPawns1);
        }
    }

    @Override
    public void resetSelection() {
        this.mouseStartPos = null;
        this.mouseEndPos = null;
        this.cachedMousePos = null;
        this.selectedPawns.clear();
    }

    @Override
    public boolean isSelecting() {
        return this.mouseStartPos != null && this.mouseEndPos != null;
    }

    @Override
    public boolean isSelectionStarted() {
        return this.mouseStartPos != null;
    }

    @Override
    public void forEachSelected(Consumer<ITargetedPawn> action) {
//        selectedPawns.forEach(action);
    }

    @Override
    public MousePos getMouseStartPos() {
        return mouseStartPos;
    }

    @Override
    public MousePos getMouseEndPos() {
        return mouseEndPos;
    }

    @Override
    public boolean isSelected(IFortressAwareEntity colonist) {
        return this.selectedPawns.contains(colonist);
    }


}
