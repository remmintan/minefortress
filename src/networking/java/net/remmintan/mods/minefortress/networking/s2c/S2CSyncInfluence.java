package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

import java.util.List;

public class S2CSyncInfluence implements FortressS2CPacket {


    public static final String CHANNEL = "sync_influence";
    private final List<BlockPos> influencePositions;

    public S2CSyncInfluence(List<BlockPos> influencePositions) {
        this.influencePositions = influencePositions;
    }

    public S2CSyncInfluence(PacketByteBuf buf) {
        this.influencePositions = buf.readList(PacketByteBuf::readBlockPos);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeCollection(influencePositions, PacketByteBuf::writeBlockPos);
    }

    @Override
    public void handle(MinecraftClient client) {
        final var provider = getManagersProvider();
        provider.get_InfluenceManager().sync(influencePositions);
    }
}
