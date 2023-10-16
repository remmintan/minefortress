package net.remmintan.mods.minefortress.core.interfaces.blueprints.world;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;

import java.util.Map;

public interface IBlueprintsWorld {

    void clearBlueprint(ServerPlayerEntity player);
    void prepareBlueprint(Map<BlockPos, BlockState> blueprintData, String blueprintFileName, int floorLevel, BlueprintGroup group);
    ServerWorld getWorld();
    void putBlueprintInAWorld(final ServerPlayerEntity player, Vec3i blueprintSize);

}
