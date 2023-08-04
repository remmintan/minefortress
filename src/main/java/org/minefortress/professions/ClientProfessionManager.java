package org.minefortress.professions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralTextContent;
import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.network.c2s.ServerboundChangeProfessionStatePacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import Z;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClientProfessionManager extends ProfessionManager {

    public ClientProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
        super(fortressManagerSupplier);
    }

    public void initProfessions(List<ProfessionFullInfo> fullInfos, String treeJson) {
        final var professionsMap = fullInfos.stream().collect(Collectors.toMap(ProfessionFullInfo::key, Profession::new));
        super.setProfessions(professionsMap);
        super.createProfessionTree(treeJson);
    }

    @Override
    public void increaseAmount(String professionId, boolean alreadyCharged) {
        if("colonist".equals(professionId)) return;
        final var profession = this.getProfession(professionId);
        final var unlockedForNormalIncrease = super.isRequirementsFulfilled(profession, CountProfessionals.INCREASE, true);
        final var unlockedForHireMenu = super.isRequirementsFulfilled(profession, CountProfessionals.DONT_COUNT, false);
        if(
            unlockedForNormalIncrease == ProfessionResearchState.UNLOCKED ||
            profession.isHireMenu() && unlockedForHireMenu == ProfessionResearchState.UNLOCKED
        ) {
            final ServerboundChangeProfessionStatePacket.AmountChange change =
                    ServerboundChangeProfessionStatePacket.AmountChange.ADD;
            final ServerboundChangeProfessionStatePacket packet =
                    new ServerboundChangeProfessionStatePacket(professionId, change);
            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_PROFESSION_STATE_CHANGE, packet);
        } else if(profession.isHireMenu() && unlockedForHireMenu == ProfessionResearchState.LOCKED_PARENT) {
            final var parent = profession.getParent();
            final var message = new LiteralTextContent("§cCan't hire " + profession.getTitle() + "§c. " +
                    "You need to unlock the " + parent.getTitle() + "§c profession first.");
            MinecraftClient.getInstance().setScreen(null);

            Optional.ofNullable(MinecraftClient.getInstance().player)
                    .ifPresent(it -> it.sendMessage(message, false));
        }
    }

    @Override
    public void decreaseAmount(String professionId) {
        if("colonist".equals(professionId)) return;
        final var profession = this.getProfession(professionId);
        final var cantRemove = profession.isHireMenu();
        if(cantRemove){
            final var message = new LiteralTextContent("§cCan't remove pawn from profession: " + profession.getTitle());
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

    public void updateProfessions(List<ProfessionEssentialInfo> info) {
        for(ProfessionEssentialInfo professionEssentialInfo : info) {
            final Profession profession = getProfession(professionEssentialInfo.getId());
            if(profession != null) {
                profession.setAmount(professionEssentialInfo.getAmount());
            }
        }
    }

}
