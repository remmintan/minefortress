package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.renderer.gui.blueprints.NetworkActionType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class ClientboundBlueprintsImportExport implements FortressClientPacket {

    private final NetworkActionType type;
    private final String path;
    private final byte[] bytes;

    public ClientboundBlueprintsImportExport(NetworkActionType type, String path, byte[] bytes) {
        this.type = type;
        this.path = path;
        this.bytes = bytes;
    }

    public ClientboundBlueprintsImportExport(PacketByteBuf buf) {
        this.type = buf.readEnumConstant(NetworkActionType.class);
        this.path = buf.readString();
        this.bytes = buf.readByteArray();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var path = Paths.get(this.path);
        final var file = path.toFile();
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Cannot create file: " + path);
            }
        }

        try(var fos = new FileOutputStream(file)) {
            fos.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to file: " + path);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(type);
        buf.writeString(path);
        buf.writeByteArray(bytes);
    }

}
