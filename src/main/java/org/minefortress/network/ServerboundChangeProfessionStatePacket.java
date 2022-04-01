package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.professions.ServerProfessionManager;

public class ServerboundChangeProfessionStatePacket implements FortressServerPacket {

    private final String professionId;
    private final AmountChange amountChange;

    public ServerboundChangeProfessionStatePacket(String professionId, AmountChange amountChange) {
        this.professionId = professionId;
        this.amountChange = amountChange;
    }

    public ServerboundChangeProfessionStatePacket(PacketByteBuf buf) {
        this.professionId = buf.readString();
        this.amountChange = AmountChange.values()[buf.readInt()];
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(professionId);
        buf.writeInt(amountChange.ordinal());
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final FortressServerPlayerEntity fortressPlayer = (FortressServerPlayerEntity) player;
        final ServerProfessionManager manager = fortressPlayer.getFortressServerManager().getServerProfessionManager();
        if (amountChange == AmountChange.ADD) {
            manager.increaseAmount(professionId);
        } else {
            manager.decreaseAmount(professionId);
        }
    }

    public enum AmountChange {
        ADD,
        REMOVE
    }

}
