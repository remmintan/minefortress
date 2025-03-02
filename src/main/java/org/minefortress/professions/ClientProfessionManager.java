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
import net.remmintan.mods.minefortress.core.utils.ClientExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.networking.c2s.C2SOpenBuildingHireScreen;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ClientProfessionManager extends ProfessionManager implements IClientProfessionManager {

    private final Supplier<IFortressManager> fortressManagerSupplier;

    public ClientProfessionManager(Supplier<IFortressManager> fortressManagerSupplier) {
        this.fortressManagerSupplier = fortressManagerSupplier;
    }

    @Override
    protected IFortressManager getFortressManager() {
        return fortressManagerSupplier.get();
    }

    @Override
    public void initProfessions(List<ProfessionFullInfo> fullInfos, String treeJson) {
        final var professionsMap = fullInfos.stream().collect(Collectors.toMap(ProfessionFullInfo::key, it -> (IProfession) new Profession(it)));
        super.setProfessions(professionsMap);
        super.createProfessionTree(treeJson);
    }

    @Override
    public void increaseAmount(String professionId) {
        if ("colonist".equals(professionId)) return;
        final var profession = this.getProfession(professionId);
        
        // Check if profession is unlocked
        final var unlocked = super.isRequirementsFulfilled(profession, CountProfessionals.DONT_COUNT);
        if (unlocked == ProfessionResearchState.UNLOCKED) {
            openBuildingHireScreen(professionId);
        } else if (unlocked == ProfessionResearchState.LOCKED_PARENT) {
            final var parent = profession.getParent();
            final var message = Text.literal("§cCan't hire " + profession.getTitle() + "§c. " +
                    "You need to unlock the " + parent.getTitle() + "§c profession first.");
            MinecraftClient.getInstance().setScreen(null);

            Optional.ofNullable(MinecraftClient.getInstance().player)
                    .ifPresent(it -> it.sendMessage(message, false));
        }
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
    boolean isCreativeFortress() {
        return ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance());
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
            final var blueprintId = requirementType.getBlueprintIds().get(requirementLevel);
            ClientModUtils.getBlueprintManager()
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
