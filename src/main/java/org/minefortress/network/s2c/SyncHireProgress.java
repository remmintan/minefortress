package org.minefortress.network.s2c;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.professions.hire.HireInfo;
import org.minefortress.renderer.gui.hire.HirePawnScreen;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class SyncHireProgress implements FortressS2CPacket {

    private static final String CHANNEL = "sync_hire_progress";

    private Map<String, HireInfo> professions;

    public SyncHireProgress(Map<String, HireInfo> professions) {
        this.professions = professions;
    }

    @SuppressWarnings("unchecked")
    public SyncHireProgress(PacketByteBuf buf) {
        try (var ois = new ObjectInputStream(new ByteBufInputStream(buf))) {
            this.professions = (Map<String, HireInfo>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        try(var stream = new ObjectOutputStream(new ByteBufOutputStream(buf))) {
            stream.writeObject(professions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        client.execute(() -> {
            final var currentScreen = client.currentScreen;
            if (currentScreen instanceof HirePawnScreen screen) {
                final var handler = screen.getHandler();
                if(handler != null) {
                    handler.sync(professions);
                }
            }
        });
    }
}
