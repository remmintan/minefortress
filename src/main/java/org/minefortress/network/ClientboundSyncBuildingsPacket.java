package org.minefortress.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.FortressBulding;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientboundSyncBuildingsPacket implements FortressClientPacket {

    private final List<Pair<BlockPos, BlockPos>> houses;

    public ClientboundSyncBuildingsPacket(Set<FortressBulding> houses) {
        this.houses = houses
                .stream()
                .map(h -> Pair.of(h.getStart(), h.getEnd()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public ClientboundSyncBuildingsPacket(PacketByteBuf buf) {
        final int housesSize = buf.readInt();
        List<Pair<BlockPos, BlockPos>> houses = new java.util.ArrayList<>();
        for (int i = 0; i < housesSize; i++) {
            houses.add(Pair.of(buf.readBlockPos(), buf.readBlockPos()));
        }
        this.houses = Collections.unmodifiableList(houses);
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            final FortressClientManager fortressClientManager = fortressClient.getFortressClientManager();
            fortressClientManager.updateBuildings(houses);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(houses.size());
        for (Pair<BlockPos, BlockPos> house : houses) {
            buf.writeBlockPos(house.getFirst());
            buf.writeBlockPos(house.getSecond());
        }
    }
}
