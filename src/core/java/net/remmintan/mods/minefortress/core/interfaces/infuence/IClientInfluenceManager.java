package net.remmintan.mods.minefortress.core.interfaces.infuence;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlockDataProvider;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureRenderInfoProvider;

import java.util.List;
import java.util.Optional;

public interface IClientInfluenceManager extends IStructureRenderInfoProvider {
    void tick();
    IBlockDataProvider getBlockDataProvider();

    Optional<WorldBorder> getFortressBorder();

    void startSelectingInfluencePosition();

    void cancelSelectingInfluencePosition();

    void sync(List<BlockPos> positions);

    void selectInfluencePosition();

    IInfluencePosStateHolder getInfluencePosStateHolder();

    void sendCaptureTaskPacket(BlockPos pos, IStructureBlockData blockData);
}
