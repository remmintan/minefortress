package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.core.interfaces.resources.IScrollableHandler;
import org.jetbrains.annotations.NotNull;

public class ServerboundScrollCurrentScreenPacket implements FortressC2SPacket {

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
    public void handle(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player) {
        if(player.currentScreenHandler instanceof IScrollableHandler handler) {
            handler.scrollItems(scrollPosition);
        }
    }
}
