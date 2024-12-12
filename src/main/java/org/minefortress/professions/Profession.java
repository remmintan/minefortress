package org.minefortress.professions;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionFullInfo;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfession;
import net.remmintan.mods.minefortress.gui.util.GuiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profession implements IProfession {

    private final String id;
    private final String title;
    private final ItemStack icon;
    private int amount = 0;
    private final AdvancementFrame type = AdvancementFrame.TASK;
    private final List<Text> description;
    private final List<Text> unlockMoreMessage;
    private final List<Text> unlockMessage;
    private final List<ItemInfo> itemsRequirement;
    private final ProfessionType requirementType;
    private final int requirementLevel;
    private final boolean hireMenu;

    private IProfession parent;
    private final List<IProfession> children = new ArrayList<>();

    public Profession(ProfessionFullInfo fullInfo) {
        this.id = fullInfo.key();
        this.title = fullInfo.title();
        this.icon = new ItemStack(fullInfo.icon());
        this.hireMenu = fullInfo.hireMenu();

        final var requirements = fullInfo.requirements();

        if(requirements != null) {
            requirementType = requirements.building().type();
            requirementLevel = requirements.building().level();

            this.itemsRequirement = requirements
                    .items()
                    .stream()
                    .map(it -> new ItemInfo(it.item(), it.count()))
                    .toList();
        } else {
            requirementType = null;
            requirementLevel = 0;
            itemsRequirement = Collections.emptyList();
        }

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.description = GuiUtils.splitTextInWordsForLength(fullInfo.description());
            this.unlockMessage = GuiUtils.splitTextInWordsForLength(fullInfo.unlockMessage());
            this.unlockMoreMessage = GuiUtils.splitTextInWordsForLength(fullInfo.unlockMoreMessage());
        } else {
            this.description = null;
            this.unlockMessage = null;
            this.unlockMoreMessage = null;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isHireMenu() {
        return hireMenu;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public AdvancementFrame getType() {
        return type;
    }

    @Override
    public void setParent(IProfession profession) {
        if(this.parent != null)
            throw new IllegalStateException("Profession already has a parent");
        this.parent = profession;
    }

    @Override
    public void addChild(IProfession profession) {
        this.children.add(profession);
    }

    @Override
    public List<IProfession> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public List<Text> getDescription() {
        return description;
    }

    @Override
    public List<Text> getUnlockMessage() {
        return unlockMessage;
    }

    @Override
    public List<Text> getUnlockMoreMessage() {
        return unlockMoreMessage;
    }

    @Override
    public IProfession getParent() {
        return parent;
    }

    @Override
    public List<ItemInfo> getItemsRequirement() {
        return itemsRequirement;
    }

    @Override
    public ProfessionType getRequirementType() {
        return requirementType;
    }

    @Override
    public int getRequirementLevel() {
        return requirementLevel;
    }

    @Override
    public NbtCompound toNbt() {
        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt("amount", amount);
        return nbtCompound;
    }

    @Override
    public void readNbt(NbtCompound nbtCompound) {
        if(nbtCompound.contains("amount"))
            amount = nbtCompound.getInt("amount");
        else
            amount = 0;
    }

}
