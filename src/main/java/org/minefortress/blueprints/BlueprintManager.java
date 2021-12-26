package org.minefortress.blueprints;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlueprintManager {

//    private static final String CURRENT_STRUCTURE = "village/plains/houses/plains_small_house_1";
    private static final String CURRENT_STRUCTURE = "village/plains/houses/plains_butcher_shop_1";

    private final MinecraftClient client;
    private BlockPos oldBlockPos;
    private Map<BlockPos, BlockState> currentStructure;

    public BlueprintManager(MinecraftClient client) {
        this.client = client;
    }

    public void tickUpdate(BlockPos blockPos) {
        if(oldBlockPos != null && oldBlockPos.equals(blockPos)) return;
        oldBlockPos = blockPos;
        this.currentStructure = null;

        final Optional<Structure> structureOpt = getStructureManager()
                .getStructure(new Identifier(CURRENT_STRUCTURE));

        if(structureOpt.isPresent()) {
            Structure structure = structureOpt.get();
            final Vec3i size = structure.getSize();
            final Vec3i centerDelta = new Vec3i(size.getX() / 2, 0, size.getZ() / 2);
            final StructurePlacementData placementData = new StructurePlacementData();
            final List<Structure.StructureBlockInfo> infos = placementData
                    .getRandomBlockInfos(structure.blockInfoLists, blockPos).getAll();
            this.currentStructure = infos.stream()
                    .collect(Collectors.toMap(inf -> inf.pos.add(blockPos).subtract(centerDelta).up(), inf -> inf.state));
        }

        final Vec3i delta = new Vec3i(10, 10, 10);
        final BlockPos start = blockPos.subtract(delta);
        final BlockPos end = blockPos.add(delta);

        getWorldRenderer().scheduleTerrainUpdate();
        getWorldRenderer().scheduleBlockRenders(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
    }

    public Map<BlockPos, BlockState> getBlueprintStates() {
        return Optional.ofNullable(currentStructure).orElse(Collections.emptyMap());
    }

    public boolean hasSelectedBlueprint() {
        return true;
    }

    private StructureManager getStructureManager() {
        if(client.getServer() != null) {
            return client.getServer().getStructureManager();
        } else {
            throw new IllegalStateException("Client has no server");
        }
    }

    private WorldRenderer getWorldRenderer() {
        return client.worldRenderer;
    }

}
