package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.renderer.gui.interfaces.ScrollableHandler;

public class ServerboundScrollCurrentScreenPacket implements FortressServerPacket {

    private final float scrollPosition;

    public ServerboundScrollCurrentScreenPacket(float scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    public ServerboundScrollCurrentScreenPacket(PacketByteBuf buf) {
        this.scrollPosition = buf.readFloat();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(scrollPosition);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(player.currentScreenHandler instanceof ScrollableHandler handler) {
            handler.scrollItems(scrollPosition);
        }
    }
}
