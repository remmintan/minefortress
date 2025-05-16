package org.minefortress.entity.ai.professions;

import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;

import java.util.Collections;
import java.util.List;
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

    private static @NotNull List<IFortressBuilding> getBuildings(Colonist colonist) {
        return ServerModUtils.getManagersProvider(colonist)
                .map(IServerManagersProvider::getBuildingsManager)
                .map(it -> it.getBuildings(ProfessionType.BLACKSMITH))
                .orElse(Collections.emptyList());
    }

    @Override
    protected Item getWorkingItem() {
        return Items.COAL;
    }

    private boolean shouldWork(Colonist colonist){
        return atLeastOneFurnaceIsBurning(colonist);
    }

    @Override
    @Nullable
    protected BlockPos getBlockPos(Colonist colonist) {
        // Use ServerModUtils to get buildings directly
        final var buildings = getBuildings(colonist);

        if (buildings.isEmpty()) {
            return null;
        }

        // Choose a random furnace in a random building
        final var randBuilding = new Random().nextInt(buildings.size());
        final var randFurnace = new Random().nextInt(buildings.get(randBuilding).getFurnacePos().size());
        return buildings.get(randBuilding).getFurnacePos().get(randFurnace);
    }

    private boolean atLeastOneFurnaceIsBurning(Colonist colonist){
        // Use ServerModUtils to get buildings directly
        final var buildings = getBuildings(colonist);
        List<BlockPos> furnaces = null;

        for (IFortressBuilding building : buildings) {
            // Attempt to get the furnace position. If it's null, find all furnaces and cache them.
            furnaces = building.getFurnacePos();

            if (null != furnaces) {
                for (BlockPos pos : building.getFurnacePos()) {
                    final var furnace = (FurnaceBlockEntity) colonist.getWorld().getBlockEntity(pos);
                    if (furnace != null && furnace.isBurning()) {
                        return true;
                    }
                }
            } else {
                // Find and cache furnaces.
                building.findFurnaces();
            }
        }

        return false;
    }
}
