package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.core.interfaces.professions.ProfessionsHireTypes;

public class ServerboundChangeProfessionStatePacket implements FortressC2SPacket {

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
        final var manager = getManagersProvider(server, player).getProfessionsManager();
        if (amountChange == AmountChange.ADD) {
            final var profession = manager.getProfession(professionId);
            if(profession.isHireMenu()) {
                final var professionsHireType = ProfessionsHireTypes
                        .getHireType(professionId)
                        .orElseThrow(() -> new RuntimeException("Hire type not found for profession: " + professionId));
                manager.openHireMenu(professionsHireType, player);
            } else {
                manager.increaseAmount(professionId, false);
            }
        } else {
            manager.decreaseAmount(professionId);
        }
    }

    public enum AmountChange {
        ADD,
        REMOVE
    }

}
