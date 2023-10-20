package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IEssentialBuildingInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.networking.registries.NetworkingReadersRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientboundSyncBuildingsPacket implements FortressS2CPacket {

    private final List<IEssentialBuildingInfo> houses;

    public ClientboundSyncBuildingsPacket(List<IEssentialBuildingInfo> houses) {
        this.houses = houses;
    }

    public ClientboundSyncBuildingsPacket(PacketByteBuf buf) {
        final var housesSize = buf.readInt();
        final var houses = new ArrayList<IEssentialBuildingInfo>();
        final var reader = NetworkingReadersRegistry.findReader(IEssentialBuildingInfo.class);
        for (int i = 0; i < housesSize; i++) {
            houses.add(reader.readBuffer(buf));
        }
        this.houses = Collections.unmodifiableList(houses);
    }

    @Override
    public void handle(MinecraftClient client) {
        final var fortressManager = getManagersProvider().get_ClientFortressManager();
        fortressManager.updateBuildings(houses);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(houses.size());
        for (IEssentialBuildingInfo house : houses) {
            house.write(buf);
        }
    }
}
