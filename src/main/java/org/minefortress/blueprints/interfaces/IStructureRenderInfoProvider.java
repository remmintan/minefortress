package org.minefortress.blueprints.interfaces;

import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.manager.BlueprintMetadata;

import java.util.Optional;

public interface IStructureRenderInfoProvider {

    boolean isSelecting();
    Optional<BlockPos> getStructureRenderPos();
    boolean canBuild();
    BlueprintMetadata getSelectedStructure();


}
