package net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public interface IServerBuildingsManager {

    void destroyBuilding(UUID id);

    void doRepairConfirmation(UUID id, ServerPlayerEntity player);

}
