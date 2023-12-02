package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

public class S2CSyncFightManager implements FortressS2CPacket {

    public static final String CHANNEL = "sync_fight_manager";

    private final int warriorsCount;

    public S2CSyncFightManager(int warriorsCount) {
        this.warriorsCount = warriorsCount;
    }

    public S2CSyncFightManager(PacketByteBuf buf) {
        this.warriorsCount = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(warriorsCount);
    }

    @Override
    public void handle(MinecraftClient client) {
        final var fightManager = getManagersProvider().get_ClientFortressManager().getFightManager();
        fightManager.sync(warriorsCount);
    }
}
