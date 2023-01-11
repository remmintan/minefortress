package org.minefortress.areas;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum ProfessionsSelectionType {

    QUARRY("Mining", Items.STONE_PICKAXE),
    LOGGING("Logging", Items.STONE_AXE),
    FARMING("Farming", Items.WHEAT_SEEDS),
    PLANTING("Planting", Items.OAK_SAPLING);

    private final String title;
    private final Item icon;

    ProfessionsSelectionType(String title, Item icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public Item getIcon() {
        return icon;
    }

}
