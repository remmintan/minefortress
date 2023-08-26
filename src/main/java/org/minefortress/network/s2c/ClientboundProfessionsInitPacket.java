package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.professions.ProfessionFullInfo;
import org.minefortress.utils.ModUtils;

import java.util.List;

public class ClientboundProfessionsInitPacket implements FortressS2CPacket {

    private final List<ProfessionFullInfo> professions;
    private final String treeJson;

    public ClientboundProfessionsInitPacket(List<ProfessionFullInfo> professions, String treeJson) {
        this.professions = professions;
        this.treeJson = treeJson;
    }

    public ClientboundProfessionsInitPacket(PacketByteBuf buf) {
        treeJson = buf.readString();
        professions = buf.readList(ProfessionFullInfo::read);
    }

    @Override
    public void handle(MinecraftClient client) {
        final var professionManager = ModUtils.getFortressClient().get_FortressClientManager().getProfessionManager();
        professionManager.initProfessions(this.professions, this.treeJson);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.treeJson);
        buf.writeVarInt(this.professions.size());
        for (ProfessionFullInfo profession : professions) {
            profession.write(buf);
        }
    }
}
