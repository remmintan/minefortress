package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.remmintan.mods.minefortress.core.dtos.professions.IProfessionEssentialInfo;
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionFullInfo;

import java.util.List;

public interface IClientProfessionManager extends IProfessionsManager {
    void initProfessions(List<ProfessionFullInfo> fullInfos, String treeJson);

    void updateProfessions(List<IProfessionEssentialInfo> info);
}
