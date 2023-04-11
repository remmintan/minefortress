package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.utils.ModUtils;

public class S2CUpdateNewInfluencePositionState implements FortressS2CPacket {

    public static final String CHANNEL = "update_new_influence_position_state";
    private final boolean isCorrect;

    public S2CUpdateNewInfluencePositionState(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public S2CUpdateNewInfluencePositionState(PacketByteBuf buf) {
        this.isCorrect = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(isCorrect);
    }

    @Override
    public void handle(MinecraftClient client) {
        final var influenceManager = ModUtils.getInfluenceManager();
    }
}
