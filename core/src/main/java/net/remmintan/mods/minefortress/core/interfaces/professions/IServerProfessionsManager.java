package net.remmintan.mods.minefortress.core.interfaces.professions;

public interface IServerProfessionsManager {

    void closeHireMenu();
    void sendHireRequestToCurrentHandler(String professionId);

}
