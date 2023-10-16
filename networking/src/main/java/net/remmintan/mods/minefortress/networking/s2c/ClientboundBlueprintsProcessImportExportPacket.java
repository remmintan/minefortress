package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import org.minefortress.renderer.gui.blueprints.ImportExportBlueprintsScreen;
import org.minefortress.renderer.gui.blueprints.NetworkActionType;
import org.minefortress.utils.ModUtils;

import java.io.IOException;
import java.nio.file.Files;

public class ClientboundBlueprintsProcessImportExportPacket implements FortressS2CPacket {

    private final CurrentScreenAction action;
    private final NetworkActionType type;
    private final String name;
    private final byte[] bytes;

    public ClientboundBlueprintsProcessImportExportPacket(CurrentScreenAction action) {
        this.action = action;
        this.type = NetworkActionType.IMPORT;
        this.name = "";
        this.bytes = new byte[0];
    }

    public ClientboundBlueprintsProcessImportExportPacket(String name, byte[] bytes) {
        this.action = CurrentScreenAction.SUCCESS;
        this.type = NetworkActionType.EXPORT;
        this.name = name;
        this.bytes = bytes;
    }

    public ClientboundBlueprintsProcessImportExportPacket(PacketByteBuf buf) {
        this.action = buf.readEnumConstant(CurrentScreenAction.class);
        this.type = buf.readEnumConstant(NetworkActionType.class);
        this.name = buf.readString();
        this.bytes = buf.readByteArray();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(action == CurrentScreenAction.FAILURE) {
            this.fail();
        } else {
            switch (type) {
                case NetworkActionType.EXPORT -> handleExport();
                case NetworkActionType.IMPORT -> handleImport();
            }
        }

    }

    private void handleImport() {
        this.success();
    }

    private void handleExport() {
        final var blueprintsFolder = ModUtils.getBlueprintsFolder();
        if(!blueprintsFolder.toFile().exists()) {
            blueprintsFolder.toFile().mkdirs();
        }
        final var path = blueprintsFolder.resolve(name);
        final var file = path.toFile();
        if(!file.exists()) {
            try {
                file.createNewFile();
                Files.write(path, bytes);
            } catch (IOException e) {
                e.printStackTrace();
                this.fail();
                return;
            }
        }

        this.success();
    }

    private void fail() {
        final var currentScreen = MinecraftClient.getInstance().currentScreen;
        if(currentScreen instanceof ImportExportBlueprintsScreen iebs) {
            iebs.fail();
        }
    }

    private void success() {
        final var currentScreen = MinecraftClient.getInstance().currentScreen;
        if(currentScreen instanceof ImportExportBlueprintsScreen iebs) {
            iebs.success();
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(action);
        buf.writeEnumConstant(type);
        buf.writeString(name);
        buf.writeByteArray(bytes);
    }

    public enum CurrentScreenAction {
        SUCCESS,
        FAILURE
    }

}
