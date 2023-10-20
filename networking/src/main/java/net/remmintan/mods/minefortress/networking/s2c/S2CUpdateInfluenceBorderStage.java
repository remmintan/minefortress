package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.border.WorldBorderStage;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

public class S2CUpdateInfluenceBorderStage implements FortressS2CPacket {

    public static final String CHANNEL = "update_influence_border_stage";
    private final WorldBorderStage worldBorderStage;

    public S2CUpdateInfluenceBorderStage(WorldBorderStage worldBorderStage) {
        this.worldBorderStage = worldBorderStage;
    }

    public S2CUpdateInfluenceBorderStage(PacketByteBuf buf) {
        this.worldBorderStage = buf.readEnumConstant(WorldBorderStage.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(worldBorderStage);
    }

    @Override
    public void handle(MinecraftClient client) {
        final var provider = getManagersProvider();
        final var influenceManager = provider.get_InfluenceManager();
        influenceManager.getInfluencePosStateHolder().setCorrect(worldBorderStage);
    }
}
