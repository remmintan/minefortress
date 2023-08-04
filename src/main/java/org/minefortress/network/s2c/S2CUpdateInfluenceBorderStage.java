package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.border.WorldBorderStage;
import org.minefortress.fight.influence.ClientInfluenceManager;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.utils.ModUtils;

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
        final var influenceManager = ModUtils.getInfluenceManager();
        influenceManager.getInfluencePosStateHolder().setCorrect(worldBorderStage);
    }
}
