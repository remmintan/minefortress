package org.minefortress.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.structure.Structure;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlueprintInfo {

    private final ChunkBuilder.BuiltChunk builtChunk;
    private final BlueprintChunkRendererRegion chunkRendererRegion;
    private final Vec3i size;
    private final ChunkBuilder chunkBuilder;

    private int rebuildCooldown;

    @NotNull
    public static BlueprintInfo create(String structureName, World world, ChunkBuilder builder) {
        final IntegratedServer server = MinecraftClient.getInstance().getServer();
        if(server == null) throw new IllegalStateException("Cannot create blueprint info without a server");
        final Identifier structureId = new Identifier(structureName);
        final Optional<Structure> structureOptional = server.getStructureManager().getStructure(structureId);
        if(structureOptional.isEmpty()) throw new IllegalStateException("Can't find structure with id: " + structureName);
        final Structure structure = structureOptional.get();

        final BlockPos chunckOrigin = BlockPos.ORIGIN;
        final BlueprintChunkRendererRegion blueprintChunkRendererRegion = BlueprintChunkRendererRegion.create(structure, world, chunckOrigin);

        final ChunkBuilder.BuiltChunk builtChunk = builder.new BuiltChunk(0);
        builtChunk.setOrigin(chunckOrigin.getX(), chunckOrigin.getY(), chunckOrigin.getZ());

        return new BlueprintInfo(builtChunk, blueprintChunkRendererRegion, structure.getSize(), builder);
    }

    private BlueprintInfo(ChunkBuilder.BuiltChunk builtChunk, BlueprintChunkRendererRegion chunkRendererRegion, Vec3i size, ChunkBuilder builder) {
        this.builtChunk = builtChunk;
        this.chunkRendererRegion = chunkRendererRegion;
        this.size = size;
        this.chunkBuilder = builder;
    }

    private void setOrigin(BlockPos pos) {
        final Vec3i originDelta = new Vec3i(-pos.getX() % 16, 16 - pos.getY() % 16, -pos.getZ() % 16);
        pos = new BlockPos(pos.getX(), pos.getY(), pos.getZ()).add(originDelta);
        this.builtChunk.setOrigin(pos.getX(), pos.getY(), pos.getZ());
        this.chunkRendererRegion.setOrigin(pos, originDelta);
    }

    public void rebuild(BlockPos pos) {
        if(rebuildCooldown-- > 0) {
            return;
        }
        rebuildCooldown = 100;

        this.setOrigin(pos);
        this.builtChunk.new RebuildTask(0, this.chunkRendererRegion)
                .run(this.chunkBuilder.buffers);
    }

    public ChunkBuilder.BuiltChunk getBuiltChunk() {
        return builtChunk;
    }

    public Vec3i getSize() {
        return size;
    }

    public BlueprintChunkRendererRegion getChunkRendererRegion() {
        return chunkRendererRegion;
    }
}
