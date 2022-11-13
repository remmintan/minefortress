package org.minefortress.professions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.interfaces.IWorkerPawn;
import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.network.s2c.ClientboundProfessionSyncPacket;
import org.minefortress.network.s2c.ClientboundProfessionsInitPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerProfessionManager extends ProfessionManager{

    private boolean initialized = false;
    private boolean needsUpdate = false;

    public ServerProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
        super(fortressManagerSupplier);
    }

    @Override
    public void increaseAmount(String professionId) {
        if(super.getFreeColonists() <= 0) return;
        final Profession profession = super.getProfession(professionId);
        if(profession == null) return;
        if(!super.isRequirementsFulfilled(profession, true)) return;

        final var fortressServerManager = (FortressServerManager) fortressManagerSupplier.get();
        final var serverResourceManager = fortressServerManager.getServerResourceManager();
        if(profession.getItemsRequirement() != null)
            serverResourceManager.removeItems(profession.getItemsRequirement());

        profession.setAmount(profession.getAmount() + 1);
        scheduleSync();
    }

    @Override
    public void decreaseAmount(String professionId) {
        final Profession profession = super.getProfession(professionId);
        if(profession == null) return;
        if(profession.getAmount() <= 0) return;

        profession.setAmount(profession.getAmount() - 1);
        scheduleSync();
    }

    public void tick(@Nullable ServerPlayerEntity player) {
        if(player == null) return;
        if(!initialized) {
            getProfessions().clear();
            final var professionsReader = new ProfessionsReader(player.server);
            final var professionFullInfos = professionsReader.readProfessions();
            final var treeJsonString = professionsReader.readTreeJson();
            professionFullInfos.forEach(it -> getProfessions().put(it.key(), new Profession(it)));
            final var packet = new ClientboundProfessionsInitPacket(professionFullInfos, treeJsonString);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_INIT, packet);
            initialized = true;
        }

        for(Profession prof : getProfessions().values()) {
            if(prof.getAmount() > 0) {
                final boolean unlocked = this.isRequirementsFulfilled(prof);
                if(!unlocked) {
                    prof.setAmount(prof.getAmount() - 1);
                    this.scheduleSync();
                }
            }
        }

        tickRemoveFromProfession();
        if(player != null && needsUpdate) {
            ClientboundProfessionSyncPacket packet = new ClientboundProfessionSyncPacket(getProfessions());
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_SYNC, packet);
            needsUpdate = false;
        }
    }

    private void tickRemoveFromProfession() {
        for(Map.Entry<String, Profession> entry : getProfessions().entrySet()) {
            final String professionId = entry.getKey();
            final Profession profession = entry.getValue();
            final List<IWorkerPawn> colonistsWithProfession = this.getColonistsWithProfession(professionId);
            final int redundantProfCount = colonistsWithProfession.size() - profession.getAmount();
            if(redundantProfCount <= 0) continue;

            final List<IWorkerPawn> colonistsToRemove = colonistsWithProfession.stream()
                    .limit(redundantProfCount)
                    .collect(Collectors.toList());
            colonistsToRemove.forEach(IWorkerPawn::resetProfession);
        }
    }

    public void scheduleSync() {
        needsUpdate = true;
    }

    public void writeToNbt(NbtCompound tag){
        getProfessions().forEach((key, value) -> tag.put(key, value.toNbt()));
    }

    public void readFromNbt(NbtCompound tag){
        for(String key : tag.getKeys()){
            final Profession profession = super.getProfession(key);
            if(profession == null) continue;
            profession.readNbt(tag.getCompound(key));
            scheduleSync();
        }
    }

    public Optional<String> getProfessionsWithAvailablePlaces() {
        for(Map.Entry<String, Profession> entry : getProfessions().entrySet()) {
            final String professionId = entry.getKey();
            final Profession profession = entry.getValue();
            if(profession.getAmount() > 0) {
                final long colonistsWithProfession = countColonistsWithProfession(professionId);
                if(colonistsWithProfession < profession.getAmount()) {
                    return Optional.of(professionId);
                }
            }
        }
        return Optional.empty();
    }

    private long countColonistsWithProfession(String professionId) {
        final FortressServerManager fortressServerManager = (FortressServerManager) super.fortressManagerSupplier.get();
        return fortressServerManager
                .getWorkers()
                .stream()
                .filter(colonist -> colonist.getProfessionId().equals(professionId))
                .count();
    }

    private List<IWorkerPawn> getColonistsWithProfession(String professionId) {
        final FortressServerManager fortressServerManager = (FortressServerManager) super.fortressManagerSupplier.get();
        return fortressServerManager
                .getWorkers()
                .stream()
                .filter(colonist -> colonist.getProfessionId().equals(professionId))
                .collect(Collectors.toList());
    }

}
