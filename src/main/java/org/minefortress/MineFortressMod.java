package org.minefortress;


import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MineFortressMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("minefortress", "scaffold_oak_planks"), FortressBlocks.SCAFFOLD_OAK_PLANKS);
    }
}
