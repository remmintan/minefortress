package org.minefortress.professions;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.interfaces.IProfessional;
import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.s2c.ClientboundProfessionSyncPacket;
import org.minefortress.network.s2c.ClientboundProfessionsInitPacket;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public class ServerProfessionManager extends ProfessionManager{

    public static final List<String> HIREABLE_PROFESSIONS = List.of(
            "warrior1",
            "warrior2",
            "archer1",
            "archer2"
    );
    public static final String PROFESSION_NBT_TAG = "professionId";

    private final ProfessionEntityTypesMapper profToEntityMapper = new ProfessionEntityTypesMapper();
    private final MinecraftServer server;
    private boolean professionsRead = false;
    private boolean professionsSent = false;
    private List<ProfessionFullInfo> professionsInfos;
    private String professionsTree;
    private boolean needsUpdate = false;
    public ServerProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier, MinecraftServer server) {
        super(fortressManagerSupplier);
        this.server = server;
    }

    public void openHireMenu(String professionId, ServerPlayerEntity player) {
//        final var packet = new S2COpenHireMenuPacket(professionId);
//        FortressServerNetworkHelper.send(player, S2COpenHireMenuPacket.CHANNEL, packet);
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
        decreaseAmount(professionId, false);
    }

    public void decreaseAmount(String professionId, boolean force) {
        final Profession profession = super.getProfession(professionId);
        if(profession == null) return;
        if(profession.getAmount() <= 0) return;
        if(profession.isCantRemove() && !force) return;

        profession.setAmount(profession.getAmount() - 1);
        scheduleSync();
    }

    public void tick(@Nullable ServerPlayerEntity player) {
        if(player == null) return;
        if(!professionsSent) {
            initProfessionsIfNeeded();
            final var packet = new ClientboundProfessionsInitPacket(professionsInfos, professionsTree);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_INIT, packet);
            professionsSent = true;
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
        if(needsUpdate) {
            ClientboundProfessionSyncPacket packet = new ClientboundProfessionSyncPacket(getProfessions());
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_SYNC, packet);
            needsUpdate = false;
        }
    }

    private void initProfessionsIfNeeded() {
        if(!professionsRead) {
            getProfessions().clear();
            final var professionsReader = new ProfessionsReader(server);
            professionsInfos = professionsReader.readProfessions();
            professionsTree = professionsReader.readTreeJson();
            professionsInfos.forEach(it -> getProfessions().put(it.key(), new Profession(it)));
            profToEntityMapper.read(server);
            professionsRead = true;
        }
    }

    public EntityType<? extends BasePawnEntity> getEntityTypeForProfession(String professionId) {
        return profToEntityMapper.getEntityTypeForProfession(professionId);
    }

    private void tickRemoveFromProfession() {
        for(Map.Entry<String, Profession> entry : getProfessions().entrySet()) {
            final String professionId = entry.getKey();
            final Profession profession = entry.getValue();
            final List<IProfessional> pawnsWithProf = this.getPawnsWithProfession(professionId);
            final int redundantProfCount = pawnsWithProf.size() - profession.getAmount();
            if(redundantProfCount <= 0) continue;

            pawnsWithProf
                    .stream()
                    .limit(redundantProfCount)
                    .forEach(IProfessional::resetProfession);
        }
    }

    private void scheduleSync() {
        needsUpdate = true;
    }

    public void writeToNbt(NbtCompound tag){
        getProfessions().forEach((key, value) -> tag.put(key, value.toNbt()));
    }

    public void readFromNbt(NbtCompound tag) {
        initProfessionsIfNeeded();
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
                final long colonistsWithProfession = countPawnsWithProfession(professionId);
                if(colonistsWithProfession < profession.getAmount()) {
                    return Optional.of(professionId);
                }
            }
        }
        return Optional.empty();
    }

    private long countPawnsWithProfession(String professionId) {
        final var fortressServerManager = (FortressServerManager) super.fortressManagerSupplier.get();
        return fortressServerManager
                .getProfessionals()
                .stream()
                .filter(colonist -> colonist.getProfessionId().equals(professionId))
                .count();
    }

    private List<IProfessional> getPawnsWithProfession(String professionId) {
        final FortressServerManager fortressServerManager = (FortressServerManager) super.fortressManagerSupplier.get();
        return fortressServerManager
                .getProfessionals()
                .stream()
                .filter(colonist -> colonist.getProfessionId().equals(professionId))
                .collect(Collectors.toList());
    }

}
