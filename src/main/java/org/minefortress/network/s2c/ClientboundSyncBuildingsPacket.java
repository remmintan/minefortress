package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.fortress.EssentialBuildingInfo;
import org.minefortress.fortress.FortressBuilding;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressS2CPacket;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientboundSyncBuildingsPacket implements FortressS2CPacket {

    private final List<EssentialBuildingInfo> houses;

    public ClientboundSyncBuildingsPacket(Set<FortressBuilding> houses) {
        this.houses = houses
                .stream()
                .map(h -> new EssentialBuildingInfo(h.getStart(), h.getEnd(), h.getRequirementId(), h.getBedsCount()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public ClientboundSyncBuildingsPacket(PacketByteBuf buf) {
        final int housesSize = buf.readInt();
        List<EssentialBuildingInfo> houses = new java.util.ArrayList<>();
        for (int i = 0; i < housesSize; i++) {
            houses.add(new EssentialBuildingInfo(buf));
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
        for (EssentialBuildingInfo house : houses) {
            house.write(buf);
        }
    }
}
