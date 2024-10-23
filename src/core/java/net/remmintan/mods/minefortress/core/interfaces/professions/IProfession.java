package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;

import java.util.List;

public interface IProfession {
    String getId();

    boolean isHireMenu();

    default boolean cantVoluntaryRemoveFromThisProfession() {
        return isHireMenu();
    }

    String getTitle();

    ItemStack getIcon();

    int getAmount();

    void setAmount(int amount);

    AdvancementFrame getType();

    void setParent(IProfession profession);

    void addChild(IProfession profession);

    List<IProfession> getChildren();

    List<Text> getDescription();

    List<Text> getUnlockMessage();

    List<Text> getUnlockMoreMessage();

    IProfession getParent();

    List<IItemInfo> getItemsRequirement();

    ProfessionType getRequirementType();

    int getRequirementLevel();

    NbtCompound toNbt();

    void readNbt(NbtCompound nbtCompound);
}
