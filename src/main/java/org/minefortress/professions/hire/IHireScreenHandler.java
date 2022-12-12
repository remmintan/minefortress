package org.minefortress.professions.hire;

import net.minecraft.item.Item;

import java.util.List;
import java.util.Map;

public interface IHireScreenHandler {

    String getName();

    List<String> getProfessions();
    int getHireProgress(String professionId);
    Map<Item, Integer> getCost(String professionId);
    Item getProfessionalHeadItem(String professionId);
    int getCurrentCount(String professionId);
    void increaseAmount(String professionId);

}
