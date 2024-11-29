package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientboundSyncBuildingsPacket implements FortressS2CPacket {

    private final List<BlockPos> buildingsPositions;

    public ClientboundSyncBuildingsPacket(List<BlockPos> buildingsPositions) {
        this.buildingsPositions = Collections.unmodifiableList(buildingsPositions);
    }

    public ClientboundSyncBuildingsPacket(PacketByteBuf buf) {
        buildingsPositions = Arrays.stream(buf.readLongArray()).mapToObj(BlockPos::fromLong).toList();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var fortressManager = getManagersProvider().get_ClientFortressManager();
        fortressManager.updateBuildings(buildingsPositions);
    }

    @Override
    public void write(PacketByteBuf buf) {
        final var positions = buildingsPositions.stream().mapToLong(BlockPos::asLong).toArray();
        buf.writeLongArray(positions);
    }
}
