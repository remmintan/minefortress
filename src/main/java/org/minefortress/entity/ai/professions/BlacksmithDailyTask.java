package org.minefortress.entity.ai.professions;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;

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
        // Use ServerModUtils to get buildings directly
        final var buildingsManager = ServerModUtils.getManagersProvider(colonist).getBuildingsManager();
        final var buildings = buildingsManager.getBuildings(ProfessionType.BLACKSMITH);

        if (buildings.isEmpty()) {
            return null;
        }

        final var i = new Random().nextInt(buildings.size());
        final var furnace = buildings.get(i).getFurnace();
        return furnace != null ? furnace.getPos() : null;
    }

    @Override
    protected Item getWorkingItem() {
        return Items.COAL;
    }

    private boolean shouldWork(Colonist colonist){
        return atLeastOneFurnaceIsBurning(colonist);
    }

    private boolean atLeastOneFurnaceIsBurning(Colonist colonist){
        // Use ServerModUtils to get buildings directly
        final var buildingsManager = ServerModUtils.getManagersProvider(colonist).getBuildingsManager();
        final var buildings = buildingsManager.getBuildings(ProfessionType.BLACKSMITH);

        for (IFortressBuilding building : buildings) {
            final var furnace = building.getFurnace();
            if (furnace != null && furnace.isBurning()) {
                return true;
            }
        }

        return false;
    }
}
