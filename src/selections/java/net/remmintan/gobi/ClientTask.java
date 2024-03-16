package net.remmintan.gobi;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTask;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class ClientTask implements IClientTask {
    private final List<BlockPos> blockPositions;
    private final Vector4f color;
    private final BiFunction<World, BlockPos, Boolean> shouldRenderBlock;

    public ClientTask(Iterable<BlockPos> blockPositions,
            Vector4f color,
            BiFunction<World, BlockPos, Boolean> shouldRenderBlock) {
        List<BlockPos> positions = new ArrayList<>();
        for(BlockPos pos: blockPositions) {
            positions.add(pos.toImmutable());
        }
        this.blockPositions = positions;
        this.color = color;
        this.shouldRenderBlock = shouldRenderBlock;
    }

    @Override
    public List<BlockPos> getBlockPositions() {
        return blockPositions;
    }

    @Override
    public Vector4f getColor() {
        return color;
    }

    @Override
    public boolean shouldRenderBlock(World world, BlockPos pos) {
        return shouldRenderBlock.apply(world, pos);
    }
}
