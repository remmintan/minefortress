package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;

import java.util.List;

public interface IProfession {
    String getId();

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

    List<ItemStack> getItemsRequirement();

    ProfessionType getRequirementType();

    int getRequirementLevel();

    NbtCompound toNbt();

    void readNbt(NbtCompound nbtCompound);
}
