package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface IStructureRenderInfoProvider {

    boolean isSelecting();
    Optional<BlockPos> getStructureRenderPos();
    boolean canBuild();
    IBlueprintMetadata getSelectedStructure();


}
