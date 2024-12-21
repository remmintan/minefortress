package org.minefortress.entity.ai.professions;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreenHandler;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

public class BlacksmithDailyTask extends AbstractStayNearBlockDailyTask{

    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Smelting");
        super.start(colonist);
    }

    @Override
    public boolean canStart(Colonist colonist) {
        return shouldWork(colonist);
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return shouldWork(colonist);
    }

    @Override
    @Nullable
    protected BlockPos getBlockPos(Colonist colonist) {
        final var buildings = colonist
                .getManagersProvider()
                .map(it -> it.getBuildingsManager().getBuildings(ProfessionType.BLACKSMITH))
                .orElse(Collections.emptyList());
        if (buildings.isEmpty()) {
            return null;
        }
        final var i = new Random().nextInt(buildings.size());
        return Optional
                .ofNullable(buildings.get(i).getFurnace())
                .map(BlockEntity::getPos)
                .orElse(null);
    }

    @Override
    protected Item getWorkingItem() {
        return Items.COAL;
    }

    private boolean shouldWork(Colonist colonist){
        return atLeastOneFurnaceIsBurning(colonist) || furnaceScreenIsOpen(colonist);
    }

    private boolean atLeastOneFurnaceIsBurning(Colonist colonist){
        final var buildings = colonist
                .getManagersProvider()
                .map(IServerManagersProvider::getBuildingsManager)
                .map(it -> it.getBuildings(ProfessionType.BLACKSMITH))
                .orElse(Collections.emptyList());
        for (IFortressBuilding building : buildings) {
            final var furnace = building.getFurnace();
            if (furnace != null && furnace.isBurning()) {
                return true;
            }
        }

        return false;
    }

    private boolean furnaceScreenIsOpen(Colonist colonist){
        return colonist.isScreenOpen(FortressFurnaceScreenHandler.class);
    }
}
