package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement;

import java.util.Optional;

public interface IProfessionsManager {
    IProfession getRootProfession();

    ProfessionResearchState isRequirementsFulfilled(IProfession profession, CountProfessionals countProfessionals, boolean countItems);

    IProfession getProfession(String id);

    Optional<IProfession> getByBuildingRequirement(IBlueprintRequirement requirement);

    boolean hasProfession(String name);

    int getFreeColonists();

    Optional<String> findIdFromProfession(IProfession profession);

    void increaseAmount(String professionId, boolean alreadyCharged);

    void decreaseAmount(String professionId);
}
