package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintRequirement;

import java.util.Optional;

public interface IProfessionsManager {
    IProfession getRootProfession();

    ProfessionResearchState isRequirementsFulfilled(IProfession profession, CountProfessionals countProfessionals, boolean countItems);

    IProfession getProfession(String id);

    Optional<IProfession> getByBuildingRequirement(BlueprintRequirement requirement);

    boolean hasProfession(String name);

    int getFreeColonists();

    Optional<String> findIdFromProfession(IProfession profession);

    void increaseAmount(String professionId, boolean alreadyCharged);

    void decreaseAmount(String professionId);
}
