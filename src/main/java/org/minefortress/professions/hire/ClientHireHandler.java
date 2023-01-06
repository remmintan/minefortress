package org.minefortress.professions.hire;

import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.network.c2s.C2SHirePawnWithScreenPacket;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.utils.ModUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientHireHandler implements IHireScreenHandler {

    private final String screenName;
    private Map<String, HireInfo> professions;

    public ClientHireHandler(String screenName, Map<String, HireInfo> professions) {
        this.screenName = screenName;
        this.professions = professions;
    }

    @Override
    public String getScreenName() {
        return screenName;
    }

    @Override
    public Set<String> getProfessions() {
        return professions.keySet();
    }

    @Override
    public int getHireProgress(String professionId) {
        return professions.get(professionId).hireProgress();
    }

    @Override
    public int getHireQueue(String professionId) {
        return professions.get(professionId).hireQueue();
    }

    @Override
    public List<ItemInfo> getCost(String professionId) {
        return professions.get(professionId).cost().stream().map(HireCost::toItemInfo).toList();
    }

    @Override
    public int getCurrentCount(String professionId) {
        return ModUtils
                .getFortressClientManager()
                .getProfessionManager()
                .getProfession(professionId)
                .getAmount();
    }

    public int getMaxCount(String professionId) {
        final var profession = ModUtils
                .getFortressClientManager()
                .getProfessionManager()
                .getProfession(professionId);
        final var buildingRequirement = profession.getBuildingRequirement();
        return ModUtils.getFortressClientManager().countBuildings(buildingRequirement) * 10;
    }

    @Override
    public void increaseAmount(String professionId) {
        final var packet = new C2SHirePawnWithScreenPacket(professionId);
        FortressClientNetworkHelper.send(C2SHirePawnWithScreenPacket.CHANNEL, packet);
    }

    @Override
    public void sync(Map<String, HireInfo> professions) {
        if(professions == null) {
            throw new IllegalArgumentException("Professions cannot be null");
        }
        this.professions = professions;
    }

}
