package org.minefortress.professions.hire;

import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireCost;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.C2SHirePawnWithScreenPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

import java.util.List;
import java.util.Map;

public final class ClientHireHandler implements IHireScreenHandler {

    private final String screenName;
    private Map<String, IHireInfo> professions;
    private List<String> additionalInfo;

    public ClientHireHandler(String screenName, Map<String, IHireInfo> professions, List<String> additionalInfo) {
        this.screenName = screenName;
        this.professions = professions;
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String getScreenName() {
        return screenName;
    }

    @Override
    public List<String> getProfessions() {
        return additionalInfo;
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
        return professions.get(professionId).cost().stream().map(IHireCost::toItemInfo).toList();
    }

    @Override
    public int getCurrentCount(String professionId) {
        return CoreModUtils
                .getFortressClientManager()
                .getProfessionManager()
                .getProfession(professionId)
                .getAmount();
    }

    public int getMaxCount(String professionId) {
        final var profession = CoreModUtils
                .getFortressClientManager()
                .getProfessionManager()
                .getProfession(professionId);
        final var requirementType = profession.getRequirementType();
        final var requirementLevel = profession.getRequirementLevel();
        return CoreModUtils.getBuildingsManager().countBuildings(requirementType, requirementLevel) * 10;
    }

    @Override
    public void increaseAmount(String professionId) {
        final var packet = new C2SHirePawnWithScreenPacket(professionId);
        FortressClientNetworkHelper.send(C2SHirePawnWithScreenPacket.CHANNEL, packet);
    }

    @Override
    public void sync(Map<String, IHireInfo> professions, List<String> additionalInfo) {
        if(professions == null) {
            throw new IllegalArgumentException("Professions cannot be null");
        }
        this.professions = professions;
        this.additionalInfo = additionalInfo;
    }

}
