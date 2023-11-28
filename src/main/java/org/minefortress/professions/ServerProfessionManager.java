package org.minefortress.professions;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.remmintan.mods.minefortress.core.dtos.professions.IProfessionEssentialInfo;
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionFullInfo;
import net.remmintan.mods.minefortress.core.interfaces.IFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IProfessional;
import net.remmintan.mods.minefortress.core.interfaces.professions.*;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundProfessionSyncPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundProfessionsInitPacket;
import net.remmintan.mods.minefortress.networking.s2c.S2COpenHireMenuPacket;
import net.remmintan.mods.minefortress.networking.s2c.SyncHireProgress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.ServerFortressManager;
import org.minefortress.professions.hire.ServerHireHandler;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public final class ServerProfessionManager extends ProfessionManager implements IServerProfessionsManager, ITickableManager, IWritableManager {
    public static final String PROFESSION_NBT_TAG = "professionId";

    private final ProfessionEntityTypesMapper profToEntityMapper = new ProfessionEntityTypesMapper();
    private final MinecraftServer server;
    private List<ProfessionFullInfo> professionsInfos;
    private String professionsTree;
    private boolean needsUpdate = false;

    private final Map<ProfessionsHireTypes, ServerHireHandler> hireHandlers = new HashMap<>();
    private ServerHireHandler currentHireHandler;

    private final Supplier<IServerManagersProvider> serverManagersProviderSupplier;

    public ServerProfessionManager(Supplier<IFortressManager> fortressManagerSupplier, Supplier<IServerManagersProvider> serverManagersProviderSupplier, MinecraftServer server) {
        super(fortressManagerSupplier);
        this.server = server;
        this.serverManagersProviderSupplier = serverManagersProviderSupplier;
    }

    public void openHireMenu(ProfessionsHireTypes hireType, ServerPlayerEntity player) {
        currentHireHandler = hireHandlers.computeIfAbsent(hireType, k -> new ServerHireHandler(k.getIds(), this));
        final var professions = currentHireHandler.getProfessions();
        final var screenName = hireType.getScreenName();
        final var packet = new S2COpenHireMenuPacket(screenName, professions);
        FortressServerNetworkHelper.send(player, S2COpenHireMenuPacket.CHANNEL, packet);
    }

    @Override
    public void closeHireMenu() {
        currentHireHandler = null;
    }

    @Override
    public void sendHireRequestToCurrentHandler(String professionId) {
        if(currentHireHandler != null) {
            final var profession = getProfession(professionId);
            if(!profession.isHireMenu()) {
                throw new IllegalArgumentException("Profession " + professionId + " is not a hire menu profession");
            }
            final var canHire = isRequirementsFulfilled(profession, CountProfessionals.INCREASE, true);
            final var abstractFortressManager = (IServerFortressManager)fortressManagerSupplier.get();
            if(canHire == ProfessionResearchState.UNLOCKED && getFreeColonists() > 0 && abstractFortressManager instanceof ServerFortressManager fsm) {
                final var resourceManager = serverManagersProviderSupplier.get().getResourceManager();
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
        final IProfession profession = super.getProfession(professionId);
        if(profession == null) return;
        if (profession.isHireMenu()) {
            if(this.fortressManagerSupplier.get() instanceof ServerFortressManager fsm && fsm.getReservedPawnsCount() <= 0) {
                LoggerFactory.getLogger(ServerProfessionManager.class).error("No reserved pawns but trying to hire a profession");
                return;
            }
        } else {
            if(super.getFreeColonists() <= 0) return;
        }
        if(super.isRequirementsFulfilled(profession, CountProfessionals.INCREASE, !itemsAlreadyCharged) != ProfessionResearchState.UNLOCKED) return;

        if(!itemsAlreadyCharged) {
            final var resourceManager = serverManagersProviderSupplier.get().getResourceManager();
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
        final IProfession profession = super.getProfession(professionId);
        if(profession == null) return;
        if(profession.getAmount() <= 0) return;
        if(profession.isHireMenu() && !force) return;

        profession.setAmount(profession.getAmount() - 1);
        scheduleSync();
    }

    @Override
    public void tick(@Nullable ServerPlayerEntity player) {
        if(player == null) return;

        hireHandlers.forEach((k, v) -> v.tick());
        if(currentHireHandler != null) {
            final var packet = new SyncHireProgress(currentHireHandler.getProfessions());
            FortressServerNetworkHelper.send(player, SyncHireProgress.CHANNEL, packet);
        }

        tickRemoveFromProfession();

        if(needsUpdate) {
            final var essentialInfos = new ArrayList<IProfessionEssentialInfo>();
            for(Map.Entry<String, IProfession> entry : getProfessions().entrySet())  {
                final ProfessionEssentialInfo professionEssentialInfo = new ProfessionEssentialInfo(entry.getKey(), entry.getValue().getAmount());
                essentialInfos.add(professionEssentialInfo);
            }

            ClientboundProfessionSyncPacket packet = new ClientboundProfessionSyncPacket(essentialInfos);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_SYNC, packet);
            needsUpdate = false;
        }
    }

    public void sendProfessions(@NotNull ServerPlayerEntity player) {
        initProfessionsIfNeeded();
        final var packet = new ClientboundProfessionsInitPacket(professionsInfos, professionsTree);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_INIT, packet);
    }

    private void initProfessionsIfNeeded() {
        if(getProfessions().isEmpty()) {
            final var professionsReader = new ProfessionsReader(server);
            professionsInfos = professionsReader.readProfessions();
            professionsTree = professionsReader.readTreeJson();
            final var professionsMap = professionsInfos
                    .stream()
                    .collect(Collectors.toMap(ProfessionFullInfo::key, it -> (IProfession)new Profession(it)));
            setProfessions(professionsMap);
            profToEntityMapper.read(server);
        }
    }

    public EntityType<? extends LivingEntity> getEntityTypeForProfession(String professionId) {
        return profToEntityMapper.getEntityTypeForProfession(professionId);
    }

    private void tickRemoveFromProfession() {
        for(Map.Entry<String, IProfession> entry : getProfessions().entrySet()) {
            final String professionId = entry.getKey();
            final IProfession profession = entry.getValue();
            final List<IProfessional> pawnsWithProf = this.getPawnsWithProfession(professionId);
            final int redundantProfCount = pawnsWithProf.size() - profession.getAmount();
            if(redundantProfCount <= 0) continue;

            pawnsWithProf
                    .stream()
                    .limit(redundantProfCount)
                    .forEach(IProfessional::resetProfession);
        }
    }

    public void scheduleSync() {
        needsUpdate = true;
    }

    @Override
    public void write(NbtCompound tag){
        NbtCompound professionTag = new NbtCompound();
        getProfessions().forEach((key, value) -> professionTag.put(key, value.toNbt()));
        tag.put("profession", professionTag);
    }

    @Override
    public void read(NbtCompound tag) {
        if (tag.contains("profession")) {
            NbtCompound professionTag = tag.getCompound("profession");
            initProfessionsIfNeeded();
            for(String key : professionTag.getKeys()){
                final IProfession profession = super.getProfession(key);
                if(profession == null) continue;
                profession.readNbt(professionTag.getCompound(key));
                scheduleSync();
            }
        }

    }

    public Optional<String> getProfessionsWithAvailablePlaces(boolean professionRequiresReservation) {
        for(Map.Entry<String, IProfession> entry : getProfessions().entrySet()) {
            final String professionId = entry.getKey();
            final IProfession profession = entry.getValue();

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
        final var fortressServerManager = (ServerFortressManager) super.fortressManagerSupplier.get();
        return fortressServerManager
                .getProfessionals()
                .stream()
                .filter(colonist -> colonist.getProfessionId().equals(professionId))
                .count();
    }

    private List<IProfessional> getPawnsWithProfession(String professionId) {
        final ServerFortressManager serverFortressManager = (ServerFortressManager) super.fortressManagerSupplier.get();
        return serverFortressManager
                .getProfessionals()
                .stream()
                .filter(colonist -> colonist.getProfessionId().equals(professionId))
                .collect(Collectors.toList());
    }

}
