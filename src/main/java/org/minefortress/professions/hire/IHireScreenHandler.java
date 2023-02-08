package org.minefortress.professions.hire;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.professions.Profession;
import org.minefortress.utils.ModUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IHireScreenHandler {

    String getScreenName();

    Set<String> getProfessions();
    int getHireProgress(String professionId);
    int getHireQueue(String professionId);
    List<ItemInfo> getCost(String professionId);
    static ItemStack getProfessionItem(String professionId) {
        return getProfession(professionId).map(Profession::getIcon).orElse(Items.PLAYER_HEAD.getDefaultStack());
    }
    int getCurrentCount(String professionId);
    int getMaxCount(String professionId);
    void increaseAmount(String professionId);
    static String getProfessionName(String professionId) {
        return getProfession(professionId)
                .map(Profession::getTitle)
                .orElse("Unknown");
    }

    @NotNull
    private static Optional<Profession> getProfession(String professionId) {
        final var manager = ModUtils.getProfessionManager();
        final var profession = manager.getProfession(professionId);
        return Optional.ofNullable(profession);
    }

    void sync(Map<String, HireInfo> professions);


}
