package net.remmintan.mods.minefortress.core.interfaces.networking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.apache.commons.lang3.NotImplementedException;

public interface FortressS2CPacket extends FortressPacket{

    default void handle(MinecraftClient client, FeatureSet enabledFeatures) {
        handle(client);
    }
    default void handle(MinecraftClient client) {
        throw new NotImplementedException("Either handle(MinecraftClient client, FeatureSet enabledFeatures) or handle(MinecraftClient client) must be implemented");
    }

}
