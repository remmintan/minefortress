package org.minefortress.fortress.automation.areas;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vector4f;

public enum ProfessionsSelectionType {
    QUARRY("Mining", Items.STONE_PICKAXE, new Vector4f(0.5f, 0.5f, 0.5f, 1.0f)),
    LOGGING("Logging", Items.STONE_AXE, new Vector4f(0.3f, 0.6f, 0.0f, 1.0f)),
    FARMING("Farming", Items.WHEAT_SEEDS, new Vector4f(0.8f, 0.6f, 0.2f, 1.0f)),
    PLANTING("Planting", Items.OAK_SAPLING, new Vector4f(0.2f, 0.8f, 0.2f, 1.0f));

    private final String title;
    private final Item icon;
    private final Vector4f color;

    ProfessionsSelectionType(String title, Item icon, Vector4f color) {
        this.title = title;
        this.icon = icon;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public Item getIcon() {
        return icon;
    }

    public Vector4f getColor() {
        return color;
    }
}

