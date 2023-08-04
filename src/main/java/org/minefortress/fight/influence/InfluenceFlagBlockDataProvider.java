package org.minefortress.fight.influence;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockRotation;
import org.minefortress.MineFortressMod;
import org.minefortress.blueprints.data.ClientStructureBlockDataProvider;
import org.minefortress.blueprints.data.StrctureBlockData;
import org.minefortress.blueprints.interfaces.IBlockDataProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InfluenceFlagBlockDataProvider implements IBlockDataProvider {

    private StrctureBlockData influenceFlagData = null;

    @Override
    public StrctureBlockData getBlockData(String fileName, BlockRotation rotation) {
        if(!fileName.equals("influence_flag"))
            throw new IllegalArgumentException("Invalid file name for influence flag: " + fileName);

        if (influenceFlagData == null) {
            final var structure = new StructureTemplate();
            final var nbt = readTheStructureNbtTag();
            structure.readNbt(nbt.getCompound("tag"));
            influenceFlagData = ClientStructureBlockDataProvider.buildStructureForClient(structure, rotation);
        }

        return influenceFlagData;
    }

    private static NbtCompound readTheStructureNbtTag() {
        final var modContainer = FabricLoader.getInstance().getModContainer(MineFortressMod.MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Mod container not found!"));
        final var path = modContainer.findPath("data/minefortress/structures/influence_flag.nbt")
                .orElseThrow(() -> new IllegalStateException("Structure file not found!"));
        try {
            final File file;
            try {
                file = path.toFile();
            } catch (UnsupportedOperationException e) {
                return NbtIo.readCompressed(Files.newInputStream(path));
            }
            return NbtIo.readCompressed(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read structure file!", e);
        }
    }
}
