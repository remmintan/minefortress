package org.minefortress.professions;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.minefortress.utils.GuiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profession {

    private final String title;
    private final ItemStack icon;
    private int amount = 0;
    private final AdvancementFrame type = AdvancementFrame.TASK;
    private final List<Text> unlockedDescription;
    private final List<Text> lockedDescription;
    private final String buildingRequirement;
    private Block blockRequirement = null;

    private Profession parent;
    private final List<Profession> children = new ArrayList<>();

    public Profession(String title, Item icon, String unlockedDescription, String lockedDescription) {
        this.title = title;
        this.icon = new ItemStack(icon);
        this.unlockedDescription = GuiUtils.splitTextInWordsForLength(unlockedDescription, 40);
        this.lockedDescription = GuiUtils.splitTextInWordsForLength(lockedDescription, 40);
        this.buildingRequirement = null;
    }
    public Profession(String title, Item icon, String unlockedDescription, String lockedDescription, String buildingRequirement) {
        this.title = title;
        this.icon = new ItemStack(icon);
        this.unlockedDescription = GuiUtils.splitTextInWordsForLength(unlockedDescription, 40);
        this.lockedDescription = GuiUtils.splitTextInWordsForLength(lockedDescription, 40);
        this.buildingRequirement = buildingRequirement;
    }

    public String getTitle() {
        return title;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public AdvancementFrame getType() {
        return type;
    }

    public void setParent(Profession profession) {
        if(this.parent != null)
            throw new IllegalStateException("Profession already has a parent");
        this.parent = profession;
    }

    public void addChild(Profession profession) {
        this.children.add(profession);
    }

    public List<Profession> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public List<Text> getUnlockedDescription() {
        return unlockedDescription;
    }

    public List<Text> getLockedDescription() {
        return lockedDescription;
    }

    public Profession getParent() {
        return parent;
    }

    public Profession setBlockRequirement(Block block) {
        this.blockRequirement = block;
        return this;
    }

    @Nullable
    public String getBuildingRequirement() {
        return buildingRequirement;
    }

    @Nullable
    public Block getBlockRequirement() {
        return blockRequirement;
    }

    public NbtCompound toNbt() {
        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt("amount", amount);
        return nbtCompound;
    }

    public void readNbt(NbtCompound nbtCompound) {
        if(nbtCompound.contains("amount"))
            amount = nbtCompound.getInt("amount");
        else
            amount = 0;
    }
}
