package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public enum BlueprintGroup {
    LIVING_HOUSES(true, Items.OAK_PLANKS, "Living Houses"),
    WORKSHOPS(true, Items.IRON_AXE, "Workshops"),
    FARMS(true, Items.WHEAT, "Farms"),
    SOCIAL_BUILDINGS(true, Items.BOOKSHELF, "Social Buildings"),
    DECORATION(true, Items.ROSE_BUSH, "Decoration");

    private final boolean topRow;
    private final ItemStack icon;
    private final Text nameText;

    BlueprintGroup(boolean topRow, Item item, String name) {
        this.topRow = topRow;
        this.icon = new ItemStack(item);
        this.nameText = Text.literal(name);
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
