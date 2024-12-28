package net.remmintan.mods.minefortress.networking.c2s;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IServerBlueprintManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IServerStructureBlockDataManager;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.networking.NetworkActionType;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.helpers.NetworkUtils;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundBlueprintsProcessImportExportPacket;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ServerboundBlueprintsImportExportPacket implements FortressC2SPacket {

    private final NetworkActionType type;
    private final String path;
    private final byte[] bytes;

    public ServerboundBlueprintsImportExportPacket(String path) {
        this.type = NetworkActionType.EXPORT;
        this.path = path;
        this.bytes = new byte[0];
    }

    public ServerboundBlueprintsImportExportPacket(byte[] bytes) {
        this.type = NetworkActionType.IMPORT;
        this.path = "";
        this.bytes = bytes;
    }

    public ServerboundBlueprintsImportExportPacket(PacketByteBuf buf) {
        this.type = buf.readEnumConstant(NetworkActionType.class);
        this.path = buf.readString();
        this.bytes = NetworkUtils.getDecompressedBytes(buf.readByteArray());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(type);
        buf.writeString(path);
        buf.writeByteArray(NetworkUtils.getCompressedBytes(bytes));
    }

    private static byte[] zipBlueprintsFolderToByteArray(NbtCompound serializedBlueprints, IServerStructureBlockDataManager structureManager) throws IOException {
        final byte[] bytes;
        try(
                final var byteArrayOutputStream = new ByteArrayOutputStream();
                final var zipOS = new ZipOutputStream(byteArrayOutputStream)
        ) {

            for (String blueprintId : serializedBlueprints.getKeys()) {
                final var structureNbtOpt = structureManager.getStructureNbt(blueprintId);
                if (structureNbtOpt.isEmpty()) continue;
                final var structureNbt = structureNbtOpt.get();
                structureNbt.put("minefortressMetadata", serializedBlueprints.getCompound(blueprintId));

                final var zipEntry = new ZipEntry(blueprintId + ".nbt");
                zipOS.putNextEntry(zipEntry);
                try (final var baos = new ByteArrayOutputStream()) {
                    NbtIo.writeCompressed(structureNbt, baos);
                    final var b = baos.toByteArray();
                    zipOS.write(b);
                }
                zipOS.closeEntry();
            }

            zipOS.finish();
            bytes = byteArrayOutputStream.toByteArray();
        }
        return bytes;
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) return;

        final var sbm = getManagersProvider(player).getBlueprintManager();
        switch (type) {
            case EXPORT -> handleExport(player, sbm);
            case IMPORT -> handleImport(player, sbm);
        }
    }

    private void handleExport(ServerPlayerEntity player, IServerBlueprintManager sbm) {
        byte[] bytes;
        try {
            final var tag = new NbtCompound();
            sbm.write(tag);
            final var serializedBlueprints = tag.getCompound("blueprintsManager").getCompound("blueprints");

            bytes = zipBlueprintsFolderToByteArray(serializedBlueprints, sbm.getBlockDataManager());
        }catch (RuntimeException | IOException exp) {
            exp.printStackTrace();
            final var packet = new ClientboundBlueprintsProcessImportExportPacket(ClientboundBlueprintsProcessImportExportPacket.CurrentScreenAction.FAILURE);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, packet);
            return;
        }

        final var packet = new ClientboundBlueprintsProcessImportExportPacket(path, bytes);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, packet);
    }

    private void handleImport(ServerPlayerEntity player, IServerBlueprintManager sbm) {
        try {
            final var blueprintsMap = new NbtCompound();
            try (
                final var bais = new ByteArrayInputStream(bytes);
                final var zis = new ZipInputStream(bais)
            ){
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    final var blueprintFileName = entry.getName();
                    final var blueprintId = StringUtils.substringBeforeLast(blueprintFileName, ".");

                    final var b = zis.readAllBytes();
                    final NbtCompound structureCompound;
                    try (final var nbtBais = new ByteArrayInputStream(b)) {
                        structureCompound = NbtIo.readCompressed(nbtBais);
                    }

                    if (structureCompound.contains("minefortressMetadata")) {
                        final var minefortressMetadata = structureCompound.getCompound("minefortressMetadata");
                        blueprintsMap.put(blueprintId, minefortressMetadata);
                        structureCompound.remove("minefortressMetadata");
                        sbm.getBlockDataManager().addOrUpdate(blueprintId, structureCompound);
                    }
                }
            }
            final var importedBlueprints = new NbtCompound();
            importedBlueprints.put("blueprints", blueprintsMap);

            final var tag = new NbtCompound();
            tag.put("blueprintsManager", importedBlueprints);
            sbm.read(tag);
            final var packet = new ClientboundBlueprintsProcessImportExportPacket(ClientboundBlueprintsProcessImportExportPacket.CurrentScreenAction.SUCCESS);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, packet);
        }catch (IOException | RuntimeException e) {
            e.printStackTrace();
            final var packet = new ClientboundBlueprintsProcessImportExportPacket(ClientboundBlueprintsProcessImportExportPacket.CurrentScreenAction.FAILURE);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, packet);
        }
    }


}
