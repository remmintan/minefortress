package org.minefortress.professions;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Profession {

    private final String title;
    private final ItemStack icon;
    private int amount = 0;
    private final AdvancementFrame type = AdvancementFrame.TASK;
    private final List<LiteralText> description = Collections.unmodifiableList(
            Stream.of(
                "Test description",
                "Test description",
                "Test description"
            )
            .map(LiteralText::new)
            .collect(Collectors.toList())
    );

    private Profession parent;
    private final List<Profession> children = new ArrayList<>();

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

    public List<LiteralText> getDescription() {
        return description;
    }
}
