package org.minefortress.network.s2c;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.professions.hire.ClientHireHandler;
import org.minefortress.professions.hire.HireInfo;
import org.minefortress.renderer.gui.hire.HirePawnScreen;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class S2COpenHireMenuPacket implements FortressS2CPacket {

    public static final String CHANNEL = "open_hire_menu";
    private final String screenName;
    private final Map<String, HireInfo> professions;

    public S2COpenHireMenuPacket(String screenName, Map<String, HireInfo> professions) {
        this.screenName = screenName;
        this.professions = professions;
    }

    @SuppressWarnings("unchecked")
    public S2COpenHireMenuPacket(PacketByteBuf buf) {
        this.screenName = buf.readString();
        try(var stream = new ObjectInputStream(new ByteBufInputStream(buf))) {
            this.professions = (Map<String, HireInfo>) stream.readObject();
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
        client.execute(() -> {
            final var handler = new ClientHireHandler(screenName, professions);
            final var screen = new HirePawnScreen(handler);
            client.setScreen(screen);
        });
    }
}
