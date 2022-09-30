package org.minefortress.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.data.FortressModDataLoader;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.interfaces.FortressServerPacket;
import org.minefortress.renderer.gui.blueprints.NetworkActionType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ServerboundBlueprintsImportExport implements FortressServerPacket {

    private final NetworkActionType type;
    private final String path;
    private final byte[] bytes;

    public ServerboundBlueprintsImportExport(String path) {
        this.type = NetworkActionType.EXPORT;
        this.path = path;
        this.bytes = new byte[0];
    }

    public ServerboundBlueprintsImportExport(byte[] bytes) {
        this.type = NetworkActionType.IMPORT;
        this.path = "";
        this.bytes = bytes;
    }

    public ServerboundBlueprintsImportExport(PacketByteBuf buf) {
        this.type = buf.readEnumConstant(NetworkActionType.class);
        this.path = buf.readString();
        this.bytes = buf.readByteArray();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(type);
        buf.writeString(path);
        buf.writeByteArray(bytes);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) return;

        if(player instanceof FortressServerPlayerEntity serverPlayer) {
            switch (type) {
                case EXPORT -> handleExport(server, player, serverPlayer);
                case IMPORT -> handleImport(server, player, serverPlayer);
            }
        }
    }

    private void handleExport(MinecraftServer server, ServerPlayerEntity player, FortressServerPlayerEntity serverPlayer) {
        byte[] bytes;
        try {
            final var sbm = serverPlayer.getServerBlueprintManager();
            sbm.write();
            final var blueprintsFolderPath = sbm.getBlockDataManager().getBlueprintsFolder();
            final var blueprintsPath = FortressModDataLoader.getFolderAbsolutePath(blueprintsFolderPath, server.session);
            bytes = zipBlueprintsFolderToByteArray(blueprintsPath);
        }catch (RuntimeException exp) {
            exp.printStackTrace();
            final var packet = new ClientboundBlueprintsProcessImportExportPacket(ClientboundBlueprintsProcessImportExportPacket.CurrentScreenAction.FAILURE);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, packet);
            return;
        }

        final var packet = new ClientboundBlueprintsProcessImportExportPacket(path, bytes);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, packet);
    }

    private void handleImport(MinecraftServer server, ServerPlayerEntity player, FortressServerPlayerEntity serverPlayer) {
        try {
            final var sbm = serverPlayer.getServerBlueprintManager();
            final var blueprintsFolderPath = sbm.getBlockDataManager().getBlueprintsFolder();
            final var pathString = FortressModDataLoader.getFolderAbsolutePath(blueprintsFolderPath, server.session);
            final var path = Paths.get(pathString);
            Files.deleteIfExists(path);
            final var target = path.toFile();
            target.mkdirs();
            try (
                final var bais = new ByteArrayInputStream(bytes);
                final var zis = new ZipInputStream(bais)
            ){
                while (zis.available() == 1) {
                    final var nextEntry = zis.getNextEntry();
                    final var file = new File(target, nextEntry.getName());
                    if (!file.toPath().normalize().startsWith(target.toPath())) {
                        throw new IOException("Bad zip entry");
                    }

                    if (nextEntry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        Files.write(file.toPath(), zis.readAllBytes());
                    }
                }
            }
            sbm.read();
            final var packet = new ClientboundBlueprintsProcessImportExportPacket(ClientboundBlueprintsProcessImportExportPacket.CurrentScreenAction.SUCCESS);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, packet);
        }catch (IOException | RuntimeException e) {
            e.printStackTrace();
            final var packet = new ClientboundBlueprintsProcessImportExportPacket(ClientboundBlueprintsProcessImportExportPacket.CurrentScreenAction.FAILURE);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, packet);
        }
    }

    private byte[] zipBlueprintsFolderToByteArray(String pathStr) {
        final var path = Path.of(pathStr);
        if(!Files.isDirectory(path)) {
            throw new RuntimeException("Path is not directory: " + pathStr);
        }

        try(
                final var byteArrayOutputStream = new ByteArrayOutputStream();
                final var zipOS = new ZipOutputStream(byteArrayOutputStream);
        ) {
            Files.walk(path).forEach(it -> {
                try {
                    final var relative = path.relativize(it);
                    final var zipEntry = new ZipEntry(relative.toString());
                    zipOS.putNextEntry(zipEntry);
                    final var bytes = Files.readAllBytes(it);
                    zipOS.write(bytes, 0, bytes.length);
                    zipOS.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return byteArrayOutputStream.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}
