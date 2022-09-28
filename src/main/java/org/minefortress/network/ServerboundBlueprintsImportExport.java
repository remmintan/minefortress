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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerboundBlueprintsImportExport implements FortressServerPacket {

    private final NetworkActionType type;
    private final String path;

    public ServerboundBlueprintsImportExport(NetworkActionType type, String path) {
        this.type = type;
        this.path = path;
    }

    public ServerboundBlueprintsImportExport(PacketByteBuf buf) {
        this.type = buf.readEnumConstant(NetworkActionType.class);
        this.path = buf.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(type);
        buf.writeString(path);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) return;

        if(player instanceof FortressServerPlayerEntity serverPlayer) {
            switch (type) {
                case EXPORT -> handleExport(server, player, serverPlayer);
            }
        }
    }

    private void handleExport(MinecraftServer server, ServerPlayerEntity player, FortressServerPlayerEntity serverPlayer) {
        final var sbm = serverPlayer.getServerBlueprintManager();
        sbm.writeToNbt();
        final var blueprintsFolderPath = sbm.getBlockDataManager().getBlueprintsFolder();
        final var blueprintsPath = FortressModDataLoader.getFolderAbsolutePath(blueprintsFolderPath, server.session);
        final var bytes = zipBlueprintsFolderToByteArray(blueprintsPath);
        final var packet = new ClientboundBlueprintsImportExport(type, path, bytes);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, packet);
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
