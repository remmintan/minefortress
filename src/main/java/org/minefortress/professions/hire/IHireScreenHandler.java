package org.minefortress.professions.hire;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfession;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IHireScreenHandler {

    String getScreenName();

    List<String> getProfessions();
    int getHireProgress(String professionId);
    int getHireQueue(String professionId);

    List<ItemInfo> getCost(String professionId);
    static ItemStack getProfessionItem(String professionId) {
        return getProfession(professionId).map(IProfession::getIcon).orElse(Items.PLAYER_HEAD.getDefaultStack());
    }
    int getCurrentCount(String professionId);
    int getMaxCount(String professionId);
    void increaseAmount(String professionId);
    static String getProfessionName(String professionId) {
        return getProfession(professionId)
                .map(IProfession::getTitle)
                .orElse("Unknown");
    }

    @NotNull
    private static Optional<IProfession> getProfession(String professionId) {
        final var manager = CoreModUtils.getProfessionManager();
        final var profession = manager.getProfession(professionId);
        return Optional.ofNullable(profession);
    }

    void sync(Map<String, IHireInfo> professions, List<String> additionalProfessionsInfo);


}
