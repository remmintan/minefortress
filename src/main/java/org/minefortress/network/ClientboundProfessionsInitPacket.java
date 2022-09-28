package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.professions.ProfessionFullInfo;
import org.minefortress.utils.ModUtils;

import java.util.ArrayList;
import java.util.List;

public class ClientboundProfessionsInitPacket implements FortressClientPacket {

    private final List<ProfessionFullInfo> professions;
    private final String treeJson;

    public ClientboundProfessionsInitPacket(List<ProfessionFullInfo> professions, String treeJson) {
        this.professions = professions;
        this.treeJson = treeJson;
    }

    public ClientboundProfessionsInitPacket(PacketByteBuf buf) {
        treeJson = buf.readString();
        final var profListSize = buf.readInt();
        professions = new ArrayList<>(profListSize);
        for (int i = 0; i < profListSize; i++) {
            professions.add(ProfessionFullInfo.read(buf));
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        final var professionManager = ModUtils.getFortressClient().getFortressClientManager().getProfessionManager();
        professionManager.initProfessions(this.professions, this.treeJson);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.treeJson);
        buf.writeInt(this.professions.size());
        for (ProfessionFullInfo profession : professions) {
            profession.write(buf);
        }
    }
}
