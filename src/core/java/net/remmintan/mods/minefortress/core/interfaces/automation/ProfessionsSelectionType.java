package net.remmintan.mods.minefortress.core.interfaces.automation;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import org.joml.Vector4f;

public enum ProfessionsSelectionType {
    QUARRY(
            "Mining",
            Items.STONE_PICKAXE,
            new Vector4f(0.5f, 0.5f, 0.5f, 1.0f),
            ProfessionType.MINER
    ),
    LOGGING(
            "Tree harvesting",
            Items.STONE_AXE,
            new Vector4f(0.3f, 0.6f, 0.0f, 1.0f),
            ProfessionType.LUMBERJACK
    ),
    FARMING(
            "Farming",
            Items.WHEAT_SEEDS,
            new Vector4f(0.8f, 0.6f, 0.2f, 1.0f),
            ProfessionType.FARMER
    );

    private final String title;
    private final Item icon;
    private final Vector4f color;
    private final ProfessionType professionType;

    ProfessionsSelectionType(String title, Item icon, Vector4f color, ProfessionType professionType) {
        this.title = title;
        this.icon = icon;
        this.color = color;
        this.professionType = professionType;
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

    public boolean satisfies(ProfessionType type) {
        return this.professionType == type;
    }
}

