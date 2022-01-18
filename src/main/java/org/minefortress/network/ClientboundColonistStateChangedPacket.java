package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.village.ColonistStateEnum;

public class ClientboundColonistStateChangedPacket implements FortressClientPacket {

    private final ColonistStateEnum stateChange;

    public ClientboundColonistStateChangedPacket(ColonistStateEnum stateChange) {
        this.stateChange = stateChange;
    }

    public ClientboundColonistStateChangedPacket(PacketByteBuf buf) {
        this.stateChange = buf.readEnumConstant(ColonistStateEnum.class);
    }

    @Override
    public void handle(MinecraftClient client) {
        final ClientWorld world = client.world;
        if(world instanceof final FortressClientWorld fortressClientWorld) {
            switch (stateChange) {
                case SPAWNED -> fortressClientWorld.getColonistsManager().addColonist();
                case REMOVED -> fortressClientWorld.getColonistsManager().removeColonist();
            }
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(stateChange);
    }
}
