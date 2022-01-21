package org.minefortress.fortress;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.ServerboundFortressCenterSetPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.tasks.BuildingManager;

public final class FortressClientManager {

    private boolean initialized = false;

    private BlockPos fortressCenter = null;
    private int colonistsCount = 0;

    private BlockPos posAppropriateForCenter;
    private BlockPos oldPosAppropriateForCenter;

    public int getColonistsCount() {
        return colonistsCount;
    }

    public void sync(int colonistsCount, BlockPos fortressCenter) {
        this.colonistsCount = colonistsCount;
        this.fortressCenter = fortressCenter;
        initialized = true;
    }

    public void tick(FortressMinecraftClient fortressClient) {
        final MinecraftClient client = (MinecraftClient) fortressClient;
        if(!initialized) return;
        if(isFortressInitializationNeeded()) {
            final BlockPos hoveredBlockPos = fortressClient.getHoveredBlockPos();
            if(hoveredBlockPos!=null) {
                if(hoveredBlockPos.equals(oldPosAppropriateForCenter)) return;

                final BlockPos cursor = hoveredBlockPos.mutableCopy();
                while (!BuildingManager.canPlaceBlock(client.world, cursor))
                    cursor.up();

                while (BuildingManager.canPlaceBlock(client.world, cursor.toImmutable().down()))
                    cursor.down();

                posAppropriateForCenter = cursor.toImmutable();
            }
        }
    }

    public BlockState getStateForCampCenter() {
        return Blocks.CAMPFIRE.getDefaultState();
    }

    public BlockPos getPosAppropriateForCenter() {
        return posAppropriateForCenter;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isFortressInitializationNeeded() {
        return initialized && fortressCenter == null;
    }

    public void setupFortressCenter() {
        if(fortressCenter!=null) throw new IllegalStateException("Fortress center already set");
        fortressCenter = posAppropriateForCenter;
        posAppropriateForCenter = null;
        final ServerboundFortressCenterSetPacket serverboundFortressCenterSetPacket = new ServerboundFortressCenterSetPacket(fortressCenter);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SET_CENTER, serverboundFortressCenterSetPacket);
    }

    public void updateRenderer(WorldRenderer worldRenderer) {
        if(oldPosAppropriateForCenter == posAppropriateForCenter) return;
        final BlockPos posAppropriateForCenter = this.getPosAppropriateForCenter();
        if(posAppropriateForCenter != null) {
            oldPosAppropriateForCenter = posAppropriateForCenter;
            final BlockPos start = posAppropriateForCenter.add(-2, -2, -2);
            final BlockPos end = posAppropriateForCenter.add(2, 2, 2);
            worldRenderer.scheduleBlockRenders(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
            worldRenderer.scheduleTerrainUpdate();
        }
    }
}
