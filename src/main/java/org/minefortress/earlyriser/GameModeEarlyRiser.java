package org.minefortress.earlyriser;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class GameModeEarlyRiser implements Runnable{

    private static final String CLASS_DESCRIPTOR_TEMPLATE = "L%s;";

    @Override
    public void run() {
        final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();

        final String gameModeClassName = mappingResolver.mapClassName("intermediary", "net.minecraft.class_1934");
        ClassTinkerers
                .enumBuilder(gameModeClassName, int.class, String.class)
                .addEnum("FORTRESS", 4, "fortress")
                .build();

        final String gameModeSelectionClassName = mappingResolver
                .mapClassName("intermediary", "net.minecraft.class_5289$class_5290");

        final String textCN = mappingResolver
                .mapClassName("intermediary", "net.minecraft.class_2561");
        final String itemStackCN = mappingResolver
                .mapClassName("intermediary", "net.minecraft.class_1799");

        final String textDescriptor = String.format(CLASS_DESCRIPTOR_TEMPLATE, textCN);
        final String itemStackDescriptor = String.format(CLASS_DESCRIPTOR_TEMPLATE, itemStackCN);

        ClassTinkerers
                .enumBuilder(gameModeSelectionClassName, textDescriptor, String.class, itemStackDescriptor)
                .addEnum("FORTRESS", () -> new Object[] {Text.of("Fortress"), "gamemode fortress", Items.RED_BED.getDefaultStack()})
                .build();
    }
}
