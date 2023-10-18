package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionEssentialInfo;
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionFullInfo;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfessionsManager;

import java.util.List;

public interface IClientProfessionManager extends IProfessionsManager {
    void initProfessions(List<ProfessionFullInfo> fullInfos, String treeJson);

    void updateProfessions(List<ProfessionEssentialInfo> info);
}
