package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.minecraft.server.network.ServerPlayerEntity;

public interface IServerProfessionsManager extends IProfessionsManager {

    void closeHireMenu();
    void sendHireRequestToCurrentHandler(String professionId);
    void openHireMenu(ProfessionsHireTypes hireType, ServerPlayerEntity player);

}
