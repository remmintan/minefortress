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
import java.util.Map;

public class S2COpenHireMenuPacket implements FortressS2CPacket {

    public static final String CHANNEL = "open_hire_menu";
    private final String screenName;
    private final Map<String, IHireInfo> professions;

    public S2COpenHireMenuPacket(String screenName, Map<String, IHireInfo> professions) {
        this.screenName = screenName;
        this.professions = professions;
    }

    @SuppressWarnings("unchecked")
    public S2COpenHireMenuPacket(PacketByteBuf buf) {
        this.screenName = buf.readString();
        try(var stream = new ObjectInputStream(new ByteBufInputStream(buf))) {
            this.professions = (Map<String, IHireInfo>) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(screenName);
        try(var stream = new ObjectOutputStream(new ByteBufOutputStream(buf))) {
            stream.writeObject(professions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        final var manager = getManagersProvider().get_ClientFortressManager();
        manager.open_HireScreen(client, screenName, professions);
    }
}
