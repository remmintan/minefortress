package org.minefortress.professions;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Profession {

    private final String title;
    private final ItemStack icon;
    private int amount = 0;
    private AdvancementFrame type = AdvancementFrame.TASK;

    public Profession(String title, Item icon) {
        this.title = title;
        this.icon = new ItemStack(icon);
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
}
