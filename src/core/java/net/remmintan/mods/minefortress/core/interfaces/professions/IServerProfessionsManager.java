package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ISyncableServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;

import java.util.Optional;

public interface IServerProfessionsManager extends IProfessionsManager, IServerManager, ISyncableServerManager, ITickableManager, IWritableManager {
    Optional<String> getProfessionsWithAvailablePlaces(boolean professionRequiresReservation);
    EntityType<? extends LivingEntity> getEntityTypeForProfession(String professionId);
    void decreaseAmount(String professionId, boolean b);
    void sendProfessions(ServerPlayerEntity player);
    void reservePawn();

}
