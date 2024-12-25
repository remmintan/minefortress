package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

import java.util.Optional;

public interface IServerProfessionsManager extends IProfessionsManager, IServerManager {
    Optional<String> getProfessionsWithAvailablePlaces(boolean professionRequiresReservation);
    EntityType<? extends LivingEntity> getEntityTypeForProfession(String professionId);
    void decreaseAmount(String professionId, boolean b);
    void scheduleSync();
    void sendProfessions(ServerPlayerEntity player);

    void reservePawn();

}
