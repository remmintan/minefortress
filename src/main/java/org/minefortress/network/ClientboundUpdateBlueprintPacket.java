package org.minefortress.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.renderer.gui.blueprints.BlueprintsScreen;
import org.minefortress.utils.ModUtils;

public class ClientboundUpdateBlueprintPacket implements FortressClientPacket {

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
        if(client instanceof FortressMinecraftClient fortressClient) {
            if(type == Type.UPDATE)
                fortressClient.getBlueprintManager().update(file, tag, newFloorLevel);
            else if(type == Type.REMOVE) {
                fortressClient.getBlueprintManager().clearStructure();
                fortressClient.getBlueprintManager().remove(file);
                final var currentScreen = MinecraftClient.getInstance().currentScreen;
                if(currentScreen instanceof BlueprintsScreen bps) {
                    bps.updateSlots();
                }
            }
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
