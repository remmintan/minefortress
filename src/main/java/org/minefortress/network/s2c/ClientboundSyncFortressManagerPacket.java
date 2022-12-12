package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.FortressGamemode;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressS2CPacket;

public class ClientboundSyncFortressManagerPacket implements FortressS2CPacket {

    private final int colonistsCount;
    private final BlockPos fortressPos;
    private final FortressGamemode fortressGamemode;
    private final int maxColonistsCount;

    public ClientboundSyncFortressManagerPacket(int colonistsCount, BlockPos fortressPos, FortressGamemode fortressGamemode,  int maxColonistsCount) {
        this.colonistsCount = colonistsCount;
        this.fortressPos = fortressPos;
        this.fortressGamemode = fortressGamemode;
        this.maxColonistsCount = maxColonistsCount;
    }

    public ClientboundSyncFortressManagerPacket(PacketByteBuf buf) {
        this.colonistsCount = buf.readInt();
        final boolean centerExists = buf.readBoolean();
        if(centerExists)
            this.fortressPos = buf.readBlockPos();
        else
            this.fortressPos = null;

        this.fortressGamemode = FortressGamemode.valueOf(buf.readString(100));
        this.maxColonistsCount = buf.readInt();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getFortressClientManager().sync(colonistsCount, fortressPos, this.fortressGamemode, this.maxColonistsCount);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(colonistsCount);
        final boolean centerExists = fortressPos != null;
        buf.writeBoolean(centerExists);
        if(centerExists)
            buf.writeBlockPos(fortressPos);

        buf.writeString(fortressGamemode.name());
        buf.writeInt(maxColonistsCount);
    }
}
