package org.minefortress.network;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.MineFortressMod;
import org.minefortress.blueprints.world.FortressServerWorld;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundFinishEditBlueprintPacket implements FortressServerPacket {

    private final boolean shouldSave;

    public ServerboundFinishEditBlueprintPacket(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public ServerboundFinishEditBlueprintPacket(PacketByteBuf buf) {
        this.shouldSave = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(this.shouldSave);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(shouldSave) {
            updateBlueprint(server, player);
        }

        final ServerWorld world = server.getWorld(World.OVERWORLD);
        player.moveToWorld(world);
    }

    private void updateBlueprint(MinecraftServer server, ServerPlayerEntity player) {
        final FortressServerWorld fortressServerWorld = (FortressServerWorld) player.world;

        final String fileName = fortressServerWorld.getFileName();
        final Identifier updatedStructureIdentifier = new Identifier(MineFortressMod.MOD_ID, fileName);
        final StructureManager structureManager = server.getStructureManager();
        final Structure structureToUpdate = structureManager.getStructureOrBlank(updatedStructureIdentifier);
        fortressServerWorld.enableSaveStructureMode();

        final BlockPos start = new BlockPos(0, 16, 0);
        final BlockPos end = new BlockPos(16, 32, 16);
        final Iterable<BlockPos> allPositions = BlockPos.iterate(start, end);

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for(BlockPos pos : allPositions) {
            final BlockState blockState = fortressServerWorld.getBlockState(pos);
            if(!blockState.isAir()) {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());

                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }
        }

        final BlockPos min = new BlockPos(minX, minY, minZ);
        final BlockPos max = new BlockPos(maxX, maxY, maxZ);
        final BlockPos dimensions = max.subtract(min).add(1, 1, 1);

        structureToUpdate.saveFromWorld(fortressServerWorld, min, dimensions, true, Blocks.AIR);
        fortressServerWorld.disableSaveStructureMode();

        if(player instanceof FortressServerPlayerEntity fortressServerPlayer) {
            final NbtCompound updatedStructure = new NbtCompound();
            structureToUpdate.writeNbt(updatedStructure);
            fortressServerPlayer.getServerBlueprintManager().update(fileName, updatedStructure);
        }


    }
}
