package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

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
        final var provider = getManagersProvider(server, player);
        final var manager = provider.getProfessionsManager();
        if (amountChange == AmountChange.ADD) {
            final var profession = manager.getProfession(professionId);
            if(profession.isHireMenu()) {
                final var requirementType = profession.getRequirementType();
                final var requirementLevel = profession.getRequirementLevel();
                final var buildings = provider.getBuildingsManager().getBuildings(requirementType, requirementLevel);
                if (!buildings.isEmpty()) {
                    final var building = buildings.get(0);
                    if (building instanceof NamedScreenHandlerFactory factory) {
                        player.openHandledScreen(factory);
                        //player.currentScreenHandler.
                        // TODO tell screen handler or building to open hire menu
                    }
                }
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
