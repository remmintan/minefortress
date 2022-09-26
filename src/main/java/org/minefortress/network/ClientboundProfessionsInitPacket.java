package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.professions.ProfessionFullInfo;
import org.minefortress.utils.ModUtils;

import java.util.List;

public class ClientboundProfessionsInitPacket implements FortressClientPacket {

    private final List<ProfessionFullInfo> professions;

    public ClientboundProfessionsInitPacket(List<ProfessionFullInfo> professions) {
        this.professions = professions;
    }

    public ClientboundProfessionsInitPacket(PacketByteBuf buf) {
        professions = buf.readList(ProfessionFullInfo::read);
    }

    @Override
    public void handle(MinecraftClient client) {
        final var professionManager = ModUtils.getFortressClient().getFortressClientManager().getProfessionManager();
        professionManager.initProfessions(this.professions);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.professions.size());
        for (ProfessionFullInfo profession : professions) {
            profession.write(buf);
        }
    }
}
