package org.minefortress.blueprints;

import net.minecraft.client.MinecraftClient;
import org.minefortress.interfaces.FortressMinecraftClient;

import java.util.Arrays;
import java.util.List;

public class BlueprintDataManager {

    private final MinecraftClient client;

    private final List<StructureInfo> structures = Arrays.asList(
            new StructureInfo("House 1", "village/plains/houses/plains_small_house_1"),
            new StructureInfo("House 2", "village/plains/houses/plains_small_house_2")
    );


    private int index = 0;

    public BlueprintDataManager(MinecraftClient client) {
        this.client = client;
    }

    public void selectFirst() {
        index = 0;
        final FortressMinecraftClient fortressClient = getFortressClient();
        fortressClient.getBlueprintManager().selectStructure(structures.get(index));
    }

    public void selectNext() {
        final FortressMinecraftClient fortressClient = getFortressClient();
        if(!fortressClient.getBlueprintManager().hasSelectedBlueprint()) return;

        index++;
        if (index >= structures.size()) {
            index = 0;
        }
        fortressClient.getBlueprintManager().selectStructure(structures.get(index));
    }

    private FortressMinecraftClient getFortressClient() {
        return (FortressMinecraftClient) this.client;
    }

}
