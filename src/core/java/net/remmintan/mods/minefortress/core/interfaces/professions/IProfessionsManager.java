package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;

import java.util.List;

public interface IProfessionsManager {
    IProfession getRootProfession();

    ProfessionResearchState isRequirementsFulfilled(IProfession profession, CountProfessionals countProfessionals, boolean countItems);

    IProfession getProfession(String id);

    List<IProfession> getProfessionsByType(ProfessionType type);

    boolean hasProfession(String name);

    int getFreeColonists();

    void increaseAmount(String professionId, boolean alreadyCharged);

    void decreaseAmount(String professionId);
}
