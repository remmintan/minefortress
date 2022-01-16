package org.minefortress.blueprints;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtElement;
import net.minecraft.structure.Structure;
import org.minefortress.interfaces.FortressMinecraftClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlueprintMetadataManager {

    private final MinecraftClient client;

    private static final List<BlueprintMetadata> STRUCTURES = Arrays.asList(
            new BlueprintMetadata("Small House 1", "village/plains/houses/plains_small_house_1", 4, Arrays.asList(Blocks.OAK_STAIRS, Blocks.OAK_PLANKS)),
            new BlueprintMetadata("Small House 2", "village/plains/houses/plains_small_house_2", 4, Collections.singletonList(Blocks.OAK_PLANKS)),
            new BlueprintMetadata("Small House 3", "village/plains/houses/plains_small_house_3", 4, Collections.singletonList(Blocks.OAK_STAIRS)),
            new BlueprintMetadata("Small House 4", "village/plains/houses/plains_small_house_4", 4, Collections.singletonList(Blocks.OAK_STAIRS)),
            new BlueprintMetadata("Medium House 1", "village/plains/houses/plains_medium_house_1", 4, Arrays.asList(Blocks.OAK_STAIRS, Blocks.OAK_PLANKS)),
            new BlueprintMetadata("Medium House 2", "village/plains/houses/plains_medium_house_2", 3, Collections.singletonList(Blocks.OAK_STAIRS)),
            new BlueprintMetadata("Big House 1", "village/plains/houses/plains_big_house_1",7 , Collections.singletonList(Blocks.OAK_PLANKS)),
            new BlueprintMetadata("Butcher Shop 1", "village/plains/houses/plains_butcher_shop_1", 3, Collections.singletonList(Blocks.OAK_STAIRS)),
            new BlueprintMetadata("Butcher Shop 2", "village/plains/houses/plains_butcher_shop_2", 4, Collections.singletonList(Blocks.OAK_STAIRS))
    );

    private int index = 0;

    public BlueprintMetadataManager(MinecraftClient client) {
        this.client = client;
    }

    public void selectFirst() {
        index = 0;
        final FortressMinecraftClient fortressClient = getFortressClient();
        fortressClient.getBlueprintManager().selectStructure(STRUCTURES.get(index));
    }

    public void selectNext() {
        final FortressMinecraftClient fortressClient = getFortressClient();
        if(!fortressClient.getBlueprintManager().hasSelectedBlueprint()) return;

        index++;
        if (index >= STRUCTURES.size()) {
            index = 0;
        }
        fortressClient.getBlueprintManager().selectStructure(STRUCTURES.get(index));
    }

    private FortressMinecraftClient getFortressClient() {
        return (FortressMinecraftClient) this.client;
    }

    public static BlueprintMetadata getByFile(String file) {
        for(BlueprintMetadata info : STRUCTURES) {
            if(info.getFile().equals(file)) {
                return info;
            }
        }
        return null;
    }

    public static Structure.StructureBlockInfo convertJigsawBlock(Structure.StructureBlockInfo inf) {
        if(inf.state.isOf(Blocks.JIGSAW)) {
            final NbtElement final_state = inf.nbt.get("final_state");
            if(final_state != null) {
                final String stateString = final_state.asString();
                BlockState blockState = null;
                try {
                    blockState = new BlockArgumentParser(new StringReader(stateString), false)
                            .parse(false)
                            .getBlockState();
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }

                if(blockState != null)
                    return new Structure.StructureBlockInfo(inf.pos, blockState, inf.nbt);
            }
        }
        return inf;
    }

}
