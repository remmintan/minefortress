package net.remmintan.mods.minefortress.networking.s2c;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class SyncHireProgress implements FortressS2CPacket {

    public static final String CHANNEL = "sync_hire_progress";

    private Map<String, IHireInfo> professions;
    private List<String> additionalProfessionsInfo;

    @SuppressWarnings("unchecked")
    public SyncHireProgress(PacketByteBuf buf) {
        try (var ois = new ObjectInputStream(new ByteBufInputStream(buf))) {
            this.professions = (Map<String, IHireInfo>) ois.readObject();
            this.additionalProfessionsInfo = (List<String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public SyncHireProgress(Map<String, IHireInfo> professions, List<String> additionalProfessionsInfo) {
        this.professions = professions;
        this.additionalProfessionsInfo = additionalProfessionsInfo;
    }

    @Override
    public void write(PacketByteBuf buf) {
        try(var stream = new ObjectOutputStream(new ByteBufOutputStream(buf))) {
            stream.writeObject(this.professions);
            stream.writeObject(this.additionalProfessionsInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        final var professions = getManagersProvider().get_ClientFortressManager().getProfessionManager();
        professions.syncCurrentScreenHandler(this.professions, this.additionalProfessionsInfo);
    }
}
