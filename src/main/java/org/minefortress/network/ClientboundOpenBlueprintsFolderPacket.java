package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;
import org.minefortress.network.interfaces.FortressClientPacket;

import java.nio.file.Path;

public class ClientboundOpenBlueprintsFolderPacket implements FortressClientPacket {

    private final String folderAbsolutePath;

    public ClientboundOpenBlueprintsFolderPacket(String folderAbsolutePath) {
        this.folderAbsolutePath = folderAbsolutePath;
    }

    public ClientboundOpenBlueprintsFolderPacket(PacketByteBuf buf) {
        this.folderAbsolutePath = buf.readString();
    }

    @Override
    public void handle(MinecraftClient client) {
        final var path = Path.of(folderAbsolutePath).toFile();
        if(path.exists()) {
            Util.getOperatingSystem().open(path);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(folderAbsolutePath);
    }
}
