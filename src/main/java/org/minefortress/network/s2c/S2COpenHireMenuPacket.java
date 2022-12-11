package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.professions.hire.IHireScreenHandler;
import org.minefortress.renderer.gui.hire.HirePawnScreen;

import java.util.List;
import java.util.Map;

public class S2COpenHireMenuPacket implements FortressS2CPacket {

    public static final String CHANNEL = "open_hire_menu";
    private final String professionId;

    public S2COpenHireMenuPacket(String professionId) {
        this.professionId = professionId;
    }

    public S2COpenHireMenuPacket(PacketByteBuf buf) {
        this.professionId = buf.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(professionId);
    }

    @Override
    public void handle(MinecraftClient client) {
        client.execute(() -> {
            final var handler = new FakeHireScreenHandler();
            final var screen = new HirePawnScreen(handler);
            client.setScreen(screen);
        });
    }

    private static class FakeHireScreenHandler implements IHireScreenHandler {

        @Override
        public String getName() {
            return "Hire Pawn";
        }

        @Override
        public List<String> getProfessions() {
            return List.of("test1", "test2");
        }

        @Override
        public int getHireProgress(String professionId) {
            return 10;
        }

        @Override
        public Map<Item, Integer> getCost(String professionId) {
            return Map.of(
                    Items.DIAMOND, 10,
                    Items.EMERALD, 20
            );
        }

        @Override
        public Item getProfessionalHeadItem(String professionId) {
            return Items.PLAYER_HEAD;
        }

        @Override
        public int getCurrentCount(String professionId) {
            return 10;
        }

        @Override
        public void increaseAmount(String professionId) {

        }
    }
}
