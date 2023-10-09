package net.remmintan.gobi;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTask;
import org.joml.Vector4f;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

public class ClientSelection implements IClientTask {

    private final Set<BlockPos> blockPositions;
    private final Vector4f color;
    private final BiFunction<World, BlockPos, Boolean> shouldRenderBlock;

    public ClientSelection(Iterable<BlockPos> blockPositions,
                           Vector4f color,
                           BiFunction<World, BlockPos, Boolean> shouldRenderBlock) {
        HashSet<BlockPos> positions = new HashSet<>();
        for(BlockPos pos: blockPositions) {
            positions.add(pos.toImmutable());
        }
        this.blockPositions = positions;
        this.color = color;
        this.shouldRenderBlock = shouldRenderBlock;
    }

    @Override
    public Set<BlockPos> getBlockPositions() {
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
