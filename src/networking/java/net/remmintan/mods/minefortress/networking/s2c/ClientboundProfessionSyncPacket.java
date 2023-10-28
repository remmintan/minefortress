package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.dtos.professions.IProfessionEssentialInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.networking.registries.NetworkingReadersRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientboundProfessionSyncPacket implements FortressS2CPacket {

    private final List<IProfessionEssentialInfo> professions;

    public ClientboundProfessionSyncPacket(List<IProfessionEssentialInfo> essentialInfos) {
        professions = Collections.unmodifiableList(essentialInfos);
    }

    public ClientboundProfessionSyncPacket(PacketByteBuf buf) {
        this.professions = new ArrayList<>();
        int size = buf.readInt();
        final var reader = NetworkingReadersRegistry.findReader(IProfessionEssentialInfo.class);
        for(int i = 0; i < size; i++) {
            final var info = reader.readBuffer(buf);
            this.professions.add(info);
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        final var fortressManager = getManagersProvider().get_ClientFortressManager();
        final var professionManager1 = fortressManager.getProfessionManager();
        professionManager1.updateProfessions(professions);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.professions.size());
        for(IProfessionEssentialInfo professionEssentialInfo : this.professions) {
            buf.writeString(professionEssentialInfo.id());
            buf.writeInt(professionEssentialInfo.amount());
        }
    }
}
