package org.minefortress.professions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.dtos.professions.IProfessionEssentialInfo;
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionFullInfo;
import net.remmintan.mods.minefortress.core.interfaces.IFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.CountProfessionals;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfession;
import net.remmintan.mods.minefortress.core.interfaces.professions.ProfessionResearchState;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.C2SOpenBuildingHireScreen;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundChangeProfessionStatePacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ClientProfessionManager extends ProfessionManager implements IClientProfessionManager {

    public ClientProfessionManager(Supplier<IFortressManager> fortressManagerSupplier) {
        super(fortressManagerSupplier);
    }

    @Override
    public void initProfessions(List<ProfessionFullInfo> fullInfos, String treeJson) {
        final var professionsMap = fullInfos.stream().collect(Collectors.toMap(ProfessionFullInfo::key, it -> (IProfession) new Profession(it)));
        super.setProfessions(professionsMap);
        super.createProfessionTree(treeJson);
    }

    @Override
    public void increaseAmount(String professionId, boolean alreadyCharged) {
        if ("colonist".equals(professionId)) return;
        final var profession = this.getProfession(professionId);
        final var unlockedForNormalIncrease = super.isRequirementsFulfilled(profession, CountProfessionals.INCREASE, true);
        final var unlockedForHireMenu = super.isRequirementsFulfilled(profession, CountProfessionals.DONT_COUNT, false);
        if (
                unlockedForNormalIncrease == ProfessionResearchState.UNLOCKED ||
                        profession.isHireMenu() && unlockedForHireMenu == ProfessionResearchState.UNLOCKED
        ) {
            final var change = ServerboundChangeProfessionStatePacket.AmountChange.ADD;
            final var packet = new ServerboundChangeProfessionStatePacket(professionId, change);
            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_PROFESSION_STATE_CHANGE, packet);
        } else if (profession.isHireMenu() && unlockedForHireMenu == ProfessionResearchState.LOCKED_PARENT) {
            final var parent = profession.getParent();
            final var message = Text.literal("§cCan't hire " + profession.getTitle() + "§c. " +
                    "You need to unlock the " + parent.getTitle() + "§c profession first.");
            MinecraftClient.getInstance().setScreen(null);

            Optional.ofNullable(MinecraftClient.getInstance().player)
                    .ifPresent(it -> it.sendMessage(message, false));
        }
    }

    @Override
    public void decreaseAmount(String professionId) {
        if ("colonist".equals(professionId)) return;
        final var profession = this.getProfession(professionId);
        final var cantRemove = profession.cantVoluntaryRemoveFromThisProfession();
        if (cantRemove) {
            final var message = Text.literal("§cCan't remove pawn from profession: " + profession.getTitle());
            MinecraftClient.getInstance().setScreen(null);
            Optional.ofNullable(MinecraftClient.getInstance().player)
                    .ifPresent(it -> it.sendMessage(message, true));
            return;
        }
        final ServerboundChangeProfessionStatePacket.AmountChange change =
                ServerboundChangeProfessionStatePacket.AmountChange.REMOVE;
        final ServerboundChangeProfessionStatePacket packet =
                new ServerboundChangeProfessionStatePacket(professionId, change);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_PROFESSION_STATE_CHANGE, packet);
    }

    @Override
    public void updateProfessions(List<IProfessionEssentialInfo> info) {
        for (var professionEssentialInfo : info) {
            final IProfession profession = getProfession(professionEssentialInfo.id());
            if (profession != null) {
                profession.setAmount(professionEssentialInfo.amount());
            }
        }
    }

    @Override
    public void openBuildingHireScreen(String professionId) {
        final var profession = this.getProfession(professionId);
        final var requirementType = profession.getRequirementType();
        final var requirementLevel = profession.getRequirementLevel();

        if (requirementLevel < 0 || requirementType == null) {
            return;
        }

        final var hasBuilding = fortressManagerSupplier.get().hasRequiredBuilding(requirementType, requirementLevel, 0);
        if (hasBuilding) {
            final var packet = new C2SOpenBuildingHireScreen(professionId);
            FortressClientNetworkHelper.send(C2SOpenBuildingHireScreen.CHANNEL, packet);
        } else {
            final var type = requirementType;
            final var level = requirementLevel;
            final var blueprintId = type.getBlueprintIds().get(level);
            CoreModUtils.getBlueprintManager()
                    .getBlueprintMetadataManager()
                    .getByBlueprintId(blueprintId)
                    .ifPresent(it -> {
                        final var message = Text.literal("§cCan't hire " + profession.getTitle() + "§c. " +
                                "You need to build a " + it.getName() + "§c first.");
                        MinecraftClient.getInstance().setScreen(null);
                        Optional.ofNullable(MinecraftClient.getInstance().player)
                                .ifPresent(p -> p.sendMessage(message, false));
                    });
        }
    }
}
