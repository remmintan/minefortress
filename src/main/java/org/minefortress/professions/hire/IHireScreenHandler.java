package org.minefortress.professions.hire;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.minefortress.professions.Profession;
import org.minefortress.utils.ModUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IHireScreenHandler {

    String getScreenName();

    List<String> getProfessions();
    int getHireProgress(String professionId);
    Map<Item, Integer> getCost(String professionId);
    static ItemStack getProfessionItem(String professionId) {
        return getProfession(professionId).map(Profession::getIcon).orElse(Items.PLAYER_HEAD.getDefaultStack());
    }
    int getCurrentCount(String professionId);
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

}
