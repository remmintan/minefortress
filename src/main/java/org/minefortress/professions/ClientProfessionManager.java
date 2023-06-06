package org.minefortress.professions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.network.c2s.ServerboundChangeProfessionStatePacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

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

    public String getIdByProfession(Profession profession) {
        return getProfessions().entrySet().stream()
                .filter(it -> it.getValue().equals(profession))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void increaseAmount(String professionId, boolean alreadyCharged) {
        if("colonist".equals(professionId)) return;
        if(
                super.isRequirementsFulfilled(this.getProfession(professionId), CountProfessionals.INCREASE, true)
                ||
                this.getProfession(professionId).isHireMenu() && super.isRequirementsFulfilled(this.getProfession(professionId), CountProfessionals.DONT_COUNT, false)
        ) {
            final ServerboundChangeProfessionStatePacket.AmountChange change =
                    ServerboundChangeProfessionStatePacket.AmountChange.ADD;
            final ServerboundChangeProfessionStatePacket packet =
                    new ServerboundChangeProfessionStatePacket(professionId, change);
            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_PROFESSION_STATE_CHANGE, packet);
        }
    }

    @Override
    public void decreaseAmount(String professionId) {
        if("colonist".equals(professionId)) return;
        final var profession = this.getProfession(professionId);
        final var cantRemove = profession.isHireMenu();
        if(cantRemove){
            final var message = new LiteralText("§cCan't remove pawn from profession: " + profession.getTitle());
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
