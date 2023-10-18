package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionFullInfo;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

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
