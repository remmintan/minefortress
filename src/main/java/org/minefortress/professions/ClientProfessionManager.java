package org.minefortress.professions;

import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.network.ServerboundChangeProfessionStatePacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

import java.util.List;
import java.util.function.Supplier;

public class ClientProfessionManager extends ProfessionManager {

    public ClientProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
        super(fortressManagerSupplier);
    }

    @Override
    public void increaseAmount(String professionId) {
        final ServerboundChangeProfessionStatePacket.AmountChange change =
                ServerboundChangeProfessionStatePacket.AmountChange.ADD;
        final ServerboundChangeProfessionStatePacket packet =
                new ServerboundChangeProfessionStatePacket(professionId, change);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_PROFESSION_STATE_CHANGE, packet);

    }

    @Override
    public void decreaseAmount(String professionId) {
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
