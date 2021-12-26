package org.minefortress.blueprints;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Optional;

public class BlueprintManager {

    private final MinecraftClient client;

    public BlueprintManager(MinecraftClient client) {
        this.client = client;

    }

    public void tickUpdate() {

    }

    public Map<BlockPos, BlockState> getBlueprintStates() {
        final StructureManager structureManager = getStructureManager();
        return null;
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

}
