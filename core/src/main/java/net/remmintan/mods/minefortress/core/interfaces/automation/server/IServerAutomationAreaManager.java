package net.remmintan.mods.minefortress.core.interfaces.automation.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;

import java.util.UUID;
import java.util.stream.Stream;

public interface IServerAutomationAreaManager {
    void addArea(IAutomationAreaInfo area);
    void removeArea(UUID id);
    void tick(ServerPlayerEntity serverPlayer);
    Stream<IAutomationArea> getByRequirement(String requirement);
    void sync();
    void write(NbtCompound tag);
    void read(NbtCompound tag);
}
