package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.networking.NetworkActionType;

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
        final var provider = getManagersProvider();
        final var blueprintManager = provider.get_BlueprintManager();

        if(action == CurrentScreenAction.FAILURE) {
            blueprintManager.handleImportExportFailure();
        } else {
            switch (type) {
                case EXPORT -> blueprintManager.handleBlueprintsExport(name, bytes);
                case IMPORT -> blueprintManager.handleBlueprintsImport();
            }
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
