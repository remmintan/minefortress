package org.minefortress.fight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.dtos.combat.MousePos;
import net.remmintan.mods.minefortress.core.interfaces.client.ISelectedColonistProvider;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientPawnsSelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.ITargetedSelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import net.remmintan.mods.minefortress.core.utils.GlobalProjectionCache;
import org.minefortress.renderer.CameraTools;
import org.minefortress.utils.ModUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public class ClientPawnsSelectionManager implements IClientPawnsSelectionManager, ITargetedSelectionManager, ISelectedColonistProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPawnsSelectionManager.class);

    private MousePos mouseStartPos;
    private MousePos mouseEndPos;

    private final List<IFortressAwareEntity> selectedPawns = new ArrayList<>();

    private Set<Vec2f> screenPositions = new HashSet<>();


    @Override
    public void startSelection(double x, double y) {
        this.resetSelection();
        this.mouseStartPos = new MousePos(x, y);
    }

    @Override
    public void endSelection(double x, double y) {
        this.mouseStartPos = null;
        this.mouseEndPos = null;
    }

    @Override
    public boolean hasSelected() {
        return !this.selectedPawns.isEmpty();
    }

    @Override
    public void updateSelection(Mouse mouse) {
        this.updateSelection(mouse.getX(), mouse.getY());
    }

    private void updateSelection(double x, double y) {
        if(!isSelectionStarted()) return;


        this.mouseEndPos = new MousePos(x, y);
        LOGGER.info("Updating selection");
        LOGGER.info("Mouse start pos: " + mouseStartPos.getX() + " " + mouseStartPos.getY());
        LOGGER.info("Mouse end pos: " + x + " " + y);

        if(GlobalProjectionCache.shouldUpdateValues("pawnsSelectionManager")) {
            LOGGER.info("Trying to update selection");
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

            final var screenPositions = CameraTools.projectToScreenSpace(entitesMap.keySet(), MinecraftClient.getInstance());
            this.screenPositions = screenPositions.keySet();
            for (Map.Entry<Vec2f, Vec3d> entry : screenPositions.entrySet()) {
                final var screenPos = entry.getKey();
                final var entityPos = entry.getValue();
                // log screen pos
                if (screenPos.x >= minX && screenPos.x <= maxX && screenPos.y >= minY && screenPos.y <= maxY) {
                    final var entity = entitesMap.get(entityPos);
                    selectedPawns.add(entity);
                }
            }
        }
    }

    @Override
    public void resetSelection() {
        this.mouseStartPos = null;
        this.mouseEndPos = null;
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
    public void forEachSelected(Consumer<IFortressAwareEntity> action) {
        selectedPawns.forEach(action);
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

    @Override
    public void forEachTargetedPawn(Consumer<ITargetedPawn> action) {
        this.forEachSelected(it -> {if(it instanceof ITargetedPawn tp) action.accept(tp);});
    }

    @Override
    public boolean isSelectingColonist() {
        final var state = ModUtils.getFortressClientManager().getState();
        return (state == FortressState.COMBAT || state == FortressState.BUILD_SELECTION || state==FortressState.BUILD_EDITING) && this.getSelectedPawn() != null;
    }

    @Override
    public LivingEntity getSelectedPawn() {
        if(selectedPawns.size() == 1) {
            final var pawn = selectedPawns.get(0);
            if(pawn instanceof LivingEntity le) {
                return le;
            }
        }
        return null;
    }
}
