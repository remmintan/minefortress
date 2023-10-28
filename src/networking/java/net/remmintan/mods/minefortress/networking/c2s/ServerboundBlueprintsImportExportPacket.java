package net.remmintan.mods.minefortress.networking.c2s;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.core.utils.ModPathUtils;
import net.remmintan.mods.minefortress.networking.NetworkActionType;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.helpers.NetworkUtils;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundBlueprintsProcessImportExportPacket;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;

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
            final var sbm = serverPlayer.get_ServerBlueprintManager();
            sbm.write();
            final var blueprintsFolderPath = sbm.getBlockDataManager().getBlueprintsFolder();
            final var blueprintsPath = ModPathUtils.getFolderAbsolutePath(blueprintsFolderPath, server.session);
            bytes = zipBlueprintsFolderToByteArray(blueprintsPath);
        }catch (RuntimeException | IOException exp) {
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
            final var sbm = serverPlayer.get_ServerBlueprintManager();
            final var blueprintsFolderPath = sbm.getBlockDataManager().getBlueprintsFolder();
            final var pathString = ModPathUtils.getFolderAbsolutePath(blueprintsFolderPath, server.session);
            final var path = Paths.get(pathString);
            final var target = path.toFile();
            FileUtils.deleteDirectory(target);
            target.mkdirs();
            try (
                final var bais = new ByteArrayInputStream(bytes);
                final var zis = new ZipInputStream(bais)
            ){
                ZipEntry nextEntry;
                while ((nextEntry = zis.getNextEntry()) != null) {
                    final var file = new File(target, nextEntry.getName());
                    if (!file.toPath().normalize().startsWith(target.toPath())) {
                        continue;
                    }

                    if (nextEntry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        try (final var fos = Files.newOutputStream(file.toPath())) {
                            IOUtils.copy(zis, fos);
                        }
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

    private byte[] zipBlueprintsFolderToByteArray(String pathStr) throws IOException {
        final var path = Path.of(pathStr);
        if(!Files.isDirectory(path)) {
            throw new RuntimeException("Path is not directory: " + pathStr);
        }

        try(
                final var byteArrayOutputStream = new ByteArrayOutputStream();
                final var zipOS = new ZipOutputStream(byteArrayOutputStream);
                final var walk = Files.walk(path)
        ) {
            for (Path it : walk.toList()) {
                final var relative = path.relativize(it);
                final var relativePathStr = relative.toString();
                if (Strings.isEmpty(relativePathStr)) continue;
                final var zipEntry = new ZipEntry(relativePathStr);
                zipOS.putNextEntry(zipEntry);
                final var bytes = Files.readAllBytes(it);
                zipOS.write(bytes, 0, bytes.length);
                zipOS.closeEntry();
            }

            return byteArrayOutputStream.toByteArray();
        }
    }

}
