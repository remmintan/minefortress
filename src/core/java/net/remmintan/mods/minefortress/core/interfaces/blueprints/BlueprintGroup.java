package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public enum BlueprintGroup {
    WORKSHOPS(true, Items.IRON_AXE, "Workshops"),
    LIVING_HOUSES(true, Items.OAK_PLANKS, "Living Houses"),
    DECORATION(true, Items.ROSE_BUSH, "Decoration"),
    FARMS(true, Items.WHEAT, "Farms", true),
    SOCIAL_BUILDINGS(true, Items.BOOKSHELF, "Social Buildings", true),
    ;

    private final boolean topRow;
    private final ItemStack icon;
    private final Text nameText;
    private final boolean legacy;

    BlueprintGroup(boolean topRow, Item item, String name) {
        this(topRow, item, name, false);
    }

    BlueprintGroup(boolean topRow, Item item, String name, boolean legacy) {
        this.topRow = topRow;
        this.icon = new ItemStack(item);
        this.nameText = Text.literal(name);
        this.legacy = legacy;
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

    public static List<BlueprintGroup> nonLegacyValues() {
        return Arrays.stream(BlueprintGroup.values()).filter(it -> !it.legacy).toList();
    }
}
