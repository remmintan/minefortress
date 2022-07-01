package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.selections.SelectionType;

public class ClientboundSyncCombatStatePacket implements FortressClientPacket {

    private final boolean isInCombat;

    public ClientboundSyncCombatStatePacket(boolean isInCombat) {
        this.isInCombat = isInCombat;
    }

    public ClientboundSyncCombatStatePacket(PacketByteBuf buf) {
        this.isInCombat = buf.readBoolean();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressClient) {
            fortressClient.getFortressClientManager().setInCombat(isInCombat);
            if(isInCombat) {
                fortressClient.getBlueprintManager().clearStructure();
                fortressClient.getSelectionManager().resetSelection();
                fortressClient.getSelectionManager().setSelectionType(SelectionType.SQUARES);
            }
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(isInCombat);
    }
}
