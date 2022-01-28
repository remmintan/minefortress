package org.minefortress.renderer.gui.blueprints;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public enum BlueprintGroup {
    MAIN(true, Items.DIRT, "Main");

    private final boolean topRow;
    private ItemStack icon;
    private Text nameText;

    BlueprintGroup(boolean topRow, Item item, String name) {
        this.topRow = topRow;
        this.icon = new ItemStack(item);
        this.nameText = new LiteralText(name);
    }

    public boolean isTopRow() {
        return topRow;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public Text getNameText() {
        return nameText;
    }
}
