package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector4f;

import java.util.Set;

public interface IClientTask {

    Set<BlockPos> getBlockPositions();

    Vector4f getColor();

    boolean shouldRenderBlock(World world, BlockPos pos);

}
