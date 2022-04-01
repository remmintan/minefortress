package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.professions.ClientProfessionManager;
import org.minefortress.professions.Profession;
import org.minefortress.professions.ProfessionEssentialInfo;
import org.minefortress.professions.ProfessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientboundProfessionSyncPacket implements FortressClientPacket {

    private final List<ProfessionEssentialInfo> professions = new ArrayList<>();

    public ClientboundProfessionSyncPacket(Map<String, Profession> professions) {
        for(Map.Entry<String, Profession> entry : professions.entrySet())  {
            final ProfessionEssentialInfo professionEssentialInfo = new ProfessionEssentialInfo();
            professionEssentialInfo.setId(entry.getKey());
            professionEssentialInfo.setAmount(entry.getValue().getAmount());

            this.professions.add(professionEssentialInfo);
        }
    }

    public ClientboundProfessionSyncPacket(PacketByteBuf buf) {
        int size = buf.readInt();
        for(int i = 0; i < size; i++) {
            final ProfessionEssentialInfo professionEssentialInfo = new ProfessionEssentialInfo();
            professionEssentialInfo.setId(buf.readString());
            professionEssentialInfo.setAmount(buf.readInt());

            this.professions.add(professionEssentialInfo);
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        final FortressMinecraftClient fortressCleint = (FortressMinecraftClient) client;
        final ProfessionManager professionManager = fortressCleint.getFortressClientManager().getProfessionManager();
        final ClientProfessionManager clientManager = (ClientProfessionManager) professionManager;
        clientManager.updateProfessions(this.professions);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.professions.size());
        for(ProfessionEssentialInfo professionEssentialInfo : this.professions) {
            buf.writeString(professionEssentialInfo.getId());
            buf.writeInt(professionEssentialInfo.getAmount());
        }
    }
}
