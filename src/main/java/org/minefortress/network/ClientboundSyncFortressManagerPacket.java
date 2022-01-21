package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

public class ClientboundSyncFortressManagerPacket implements FortressClientPacket {

    private final int colonistsCount;
    private final BlockPos fortressPos;

    public ClientboundSyncFortressManagerPacket(int colonistsCount, BlockPos fortressPos) {
        this.colonistsCount = colonistsCount;
        this.fortressPos = fortressPos;
    }

    public ClientboundSyncFortressManagerPacket(PacketByteBuf buf) {
        this.colonistsCount = buf.readInt();
        this.fortressPos = buf.readBlockPos();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getFortressClientManager().sync(colonistsCount, fortressPos);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(colonistsCount);
        buf.writeBlockPos(fortressPos);
    }
}
