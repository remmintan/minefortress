package org.minefortress.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.structure.Structure;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BlueprintInfo {

    private final ChunkBuilder.BuiltChunk builtChunk;
    private final BlueprintChunkRendererRegion chunkRendererRegion;
    private final ChunkBuilder chunkBuilder;
    private CompletableFuture currentRebuildTask;

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

        return new BlueprintInfo(builtChunk, blueprintChunkRendererRegion, builder);
    }

    private BlueprintInfo(ChunkBuilder.BuiltChunk builtChunk, BlueprintChunkRendererRegion chunkRendererRegion, ChunkBuilder builder) {
        this.builtChunk = builtChunk;
        this.chunkRendererRegion = chunkRendererRegion;
        this.chunkBuilder = builder;
    }

    private void setOrigin(BlockPos pos) {
        pos = new BlockPos(pos.getX() + (16 - pos.getX()%16), pos.getY() + 16, pos.getZ() + (16 - pos.getZ()%16));

        this.builtChunk.setOrigin(pos.getX(), pos.getY(), pos.getZ());
        this.chunkRendererRegion.setOrigin(pos);
    }

    public void rebuild(BlockPos pos) {
        if(currentRebuildTask != null) return;

        this.setOrigin(pos);
        this.currentRebuildTask = this.builtChunk.new RebuildTask(0, this.chunkRendererRegion)
                .run(this.chunkBuilder.buffers);
    }

    public ChunkBuilder.BuiltChunk getBuiltChunk() {
        return builtChunk;
    }
}
