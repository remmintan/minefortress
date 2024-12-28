package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;

import java.util.Optional;

public interface IStructureRenderInfoProvider {

    boolean isSelecting();
    Optional<BlockPos> getStructureRenderPos();
    boolean canBuild();

    boolean intersectsUpgradingBuilding();

    BlueprintMetadata getSelectedStructure();

    IBlueprintRotation getSelectedRotation();


}
