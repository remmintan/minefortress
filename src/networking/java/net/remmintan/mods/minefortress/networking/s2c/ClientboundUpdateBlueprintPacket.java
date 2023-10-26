package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

public class ClientboundUpdateBlueprintPacket implements FortressS2CPacket {

    private final String file;
    private final int newFloorLevel;
    private final NbtCompound tag;
    private final Type type;

    private ClientboundUpdateBlueprintPacket(String file, int newFloorLevel, NbtCompound tag, Type type) {
        this.file = file;
        this.newFloorLevel = newFloorLevel;
        this.tag = tag;
        this.type = type;
    }

    public ClientboundUpdateBlueprintPacket(PacketByteBuf buf) {
        file = buf.readString();
        newFloorLevel = buf.readInt();
        tag = buf.readNbt();
        type = buf.readEnumConstant(Type.class);
    }

    @Override
    public void handle(MinecraftClient client) {
        final var provider = getManagersProvider();
        final var blueprintManager = provider.get_BlueprintManager();

        if(type == Type.UPDATE)
            blueprintManager.update(file, tag, newFloorLevel);
        else if(type == Type.REMOVE) {
            blueprintManager.clearStructure();
            blueprintManager.remove(file);
            blueprintManager.updateSlotsInBlueprintsScreen();
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(file);
        buf.writeInt(newFloorLevel);
        buf.writeNbt(tag);
        buf.writeEnumConstant(type);
    }

    public static ClientboundUpdateBlueprintPacket edit(String file, int newFloorLevel, NbtCompound tag) {
        return new ClientboundUpdateBlueprintPacket(file, newFloorLevel, tag, Type.UPDATE);
    }

    public static ClientboundUpdateBlueprintPacket remove(String file) {
        return new ClientboundUpdateBlueprintPacket(file, 0, new NbtCompound(), Type.REMOVE);
    }

    private enum Type {
        UPDATE, REMOVE
    }

}
