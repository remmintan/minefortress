package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public interface IServerProfessionsManager extends IProfessionsManager {
    Optional<String> getProfessionsWithAvailablePlaces(boolean professionRequiresReservation);
    void closeHireMenu();
    void sendHireRequestToCurrentHandler(String professionId);
    void openHireMenu(ProfessionsHireTypes hireType, ServerPlayerEntity player);
    EntityType<? extends LivingEntity> getEntityTypeForProfession(String professionId);

}
