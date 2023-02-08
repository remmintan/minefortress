package org.minefortress.professions;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.interfaces.IProfessional;
import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.server.ServerResourceManager;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.s2c.ClientboundProfessionSyncPacket;
import org.minefortress.network.s2c.ClientboundProfessionsInitPacket;
import org.minefortress.network.s2c.S2COpenHireMenuPacket;
import org.minefortress.network.s2c.SyncHireProgress;
import org.minefortress.professions.hire.ProfessionsHireTypes;
import org.minefortress.professions.hire.ServerHireHandler;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public class ServerProfessionManager extends ProfessionManager{
    public static final String PROFESSION_NBT_TAG = "professionId";

    private final ProfessionEntityTypesMapper profToEntityMapper = new ProfessionEntityTypesMapper();
    private final MinecraftServer server;
    private boolean professionsRead = false;
    private List<ProfessionFullInfo> professionsInfos;
    private String professionsTree;
    private boolean needsUpdate = false;

    private final Map<ProfessionsHireTypes, ServerHireHandler> hireHandlers = new HashMap<>();
    private ServerHireHandler currentHireHandler;
    public ServerProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier, MinecraftServer server) {
        super(fortressManagerSupplier);
        this.server = server;
    }

    public void openHireMenu(ProfessionsHireTypes hireType, ServerPlayerEntity player) {
        currentHireHandler = hireHandlers.computeIfAbsent(hireType, k -> new ServerHireHandler(k.getIds(), this));
        final var packet = new S2COpenHireMenuPacket(hireType.getScreenName(), currentHireHandler.getProfessions());
        FortressServerNetworkHelper.send(player, S2COpenHireMenuPacket.CHANNEL, packet);
    }

    public void closeHireMenu() {
        currentHireHandler = null;
    }

    public void sendHireRequestToCurrentHandler(String professionId) {
        if(currentHireHandler != null) {
            final var profession = getProfession(professionId);
            if(!profession.isHireMenu()) {
                throw new IllegalArgumentException("Profession " + professionId + " is not a hire menu profession");
            }
            final var canHire = isRequirementsFulfilled(profession, CountProfessionals.INCREASE, true);
            final var abstractFortressManager = fortressManagerSupplier.get();
            if(canHire && getFreeColonists() > 0 && abstractFortressManager instanceof FortressServerManager fsm) {
                final var resourceManager = (ServerResourceManager) abstractFortressManager
                        .getResourceManager();
                resourceManager.removeItems(profession.getItemsRequirement());
                fsm.getPawnWithoutAProfession().ifPresent(Colonist::reserveColonist);
                fsm.scheduleSync();
                currentHireHandler.hire(professionId);
            }
        } else {
            throw new IllegalStateException("No current hire handler");
        }
    }

    @Override
    public void increaseAmount(String professionId, boolean itemsAlreadyCharged) {
        final Profession profession = super.getProfession(professionId);
        if(profession == null) return;
        if (profession.isHireMenu()) {
            if(this.fortressManagerSupplier.get() instanceof FortressServerManager fsm && fsm.getReservedPawnsCount() <= 0) {
                LoggerFactory.getLogger(ServerProfessionManager.class).error("No reserved pawns but trying to hire a profession");
                return;
            }
        } else {
            if(super.getFreeColonists() <= 0) return;
        }
        if(!super.isRequirementsFulfilled(profession, CountProfessionals.INCREASE, !itemsAlreadyCharged)) return;

        if(!itemsAlreadyCharged) {
            final var resourceManager = (ServerResourceManager) fortressManagerSupplier
                    .get()
                    .getResourceManager();
            resourceManager.removeItems(profession.getItemsRequirement());
        }

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
        if(profession.isHireMenu() && !force) return;

        profession.setAmount(profession.getAmount() - 1);
        scheduleSync();
    }

    public void tick(@Nullable ServerPlayerEntity player) {
        if(player == null) return;

        hireHandlers.forEach((k, v) -> v.tick());
        if(currentHireHandler != null) {
            final var packet = new SyncHireProgress(currentHireHandler.getProfessions());
            FortressServerNetworkHelper.send(player, SyncHireProgress.CHANNEL, packet);
        }

        tickCheckProfessionRequirements();
        tickRemoveFromProfession();

        if(needsUpdate) {
            ClientboundProfessionSyncPacket packet = new ClientboundProfessionSyncPacket(getProfessions());
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_SYNC, packet);
            needsUpdate = false;
        }
    }

    private void tickCheckProfessionRequirements() {
        for(Profession prof : getProfessions().values()) {
            if(prof.getAmount() > 0) {
                final boolean unlocked = isRequirementsFulfilled(prof, CountProfessionals.KEEP, false);
                if(!unlocked) {
                    prof.setAmount(prof.getAmount() - 1);
                    this.scheduleSync();
                }
            }
        }
    }

    public void sendProfessions(@NotNull ServerPlayerEntity player) {
        initProfessionsIfNeeded();
        final var packet = new ClientboundProfessionsInitPacket(professionsInfos, professionsTree);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_INIT, packet);
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

    public Optional<String> getProfessionsWithAvailablePlaces(boolean professionRequiresReservation) {
        for(Map.Entry<String, Profession> entry : getProfessions().entrySet()) {
            final String professionId = entry.getKey();
            final Profession profession = entry.getValue();

            if(professionRequiresReservation && !profession.isHireMenu()) continue;
            if(!professionRequiresReservation && profession.isHireMenu()) continue;

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
