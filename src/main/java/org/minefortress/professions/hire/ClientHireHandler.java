package org.minefortress.professions.hire;

import org.minefortress.fortress.resources.ItemInfo;
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
    public List<ItemInfo> getCost(String professionId) {
        return professions.get(professionId).cost();
    }

    @Override
    public int getCurrentCount(String professionId) {
        return ModUtils
                .getFortressClientManager()
                .getProfessionManager()
                .getProfession(professionId)
                .getAmount();
    }

    @Override
    public void increaseAmount(String professionId) {

    }

    @Override
    public void sync(Map<String, HireInfo> professions) {
        if(professions == null) {
            throw new IllegalArgumentException("Professions cannot be null");
        }
        this.professions = professions;
    }

}
