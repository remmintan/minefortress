package org.minefortress.fight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.dtos.combat.MousePos;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightSelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import org.minefortress.renderer.CameraTools;
import org.minefortress.utils.ModUtils;

import java.util.*;
import java.util.function.Consumer;

public class ClientFightSelectionManager implements IClientFightSelectionManager {

    public static final int RAY_LENGTH = 200;
    private MousePos mouseStartPos;
    private MousePos mouseEndPos;
    private MousePos cachedMousePos;

    private final List<IFortressAwareEntity> selectedPawns = new ArrayList<>();

    private Set<Vec2f> screenPositions = new HashSet<>();


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


            final var world = MinecraftClient.getInstance().world;

            final Map<Vec3d, IFortressAwareEntity> entitesMap = new HashMap<>();
            world
                .getEntities()
                .forEach(
                    entity -> {
                        if(entity instanceof IFortressAwareEntity fae) {
                            final var pos = entity.getPos();
                            entitesMap.put(pos, fae);
                        }
                    }
                );

            //  found min and max x and y
            int minX = Math.min(mouseStartPos.getX(), mouseEndPos.getX());
            int maxX = Math.max(mouseStartPos.getX(), mouseEndPos.getX());
            int minY = Math.min(mouseStartPos.getY(), mouseEndPos.getY());
            int maxY = Math.max(mouseStartPos.getY(), mouseEndPos.getY());


            // log max and min x and y in one line
//            LoggerFactory.getLogger(ClientFightSelectionManager.class).info("minX: " + minX + ", maxX: " + maxX + ", minY: " + minY + ", maxY: " + maxY);

            final var screenPositions = CameraTools.projectToScreenSpace(entitesMap.keySet(), MinecraftClient.getInstance());
            this.screenPositions = screenPositions.keySet();
            for (Map.Entry<Vec2f, Vec3d> entry : screenPositions.entrySet()) {
                final var screenPos = entry.getKey();
                final var entityPos = entry.getValue();
                // log screen pos
//                LoggerFactory.getLogger(ClientFightSelectionManager.class).info("screenPos: " + screenPos.x + ", " + screenPos.y);
                if (screenPos.x >= minX && screenPos.x <= maxX && screenPos.y >= minY && screenPos.y <= maxY) {
                    final var entity = entitesMap.get(entityPos);
                    selectedPawns.add(entity);
                }
            }

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

    @Override
    public Set<Vec2f> getScreenPositions() {
        return screenPositions;
    }

}
