package net.remmintan.mods.minefortress.core.interfaces.automation;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.joml.Vector4f;

import java.util.function.Predicate;

public enum ProfessionsSelectionType {
    QUARRY(
            "Mining",
            Items.STONE_PICKAXE,
            new Vector4f(0.5f, 0.5f, 0.5f, 1.0f),
            (reqString) -> reqString.startsWith("miner")
    ),
//    LOGGING(
//            "Logging",
//            Items.STONE_AXE,
//            new Vector4f(0.3f, 0.6f, 0.0f, 1.0f),
//            (reqString) -> reqString.startsWith("lumberjack")
//    ),
    FARMING(
            "Farming",
            Items.WHEAT_SEEDS,
            new Vector4f(0.8f, 0.6f, 0.2f, 1.0f),
            (reqString) -> reqString.startsWith("farm")
    );
//    PLANTING(
//            "Planting",
//            Items.OAK_SAPLING,
//            new Vector4f(0.2f, 0.8f, 0.2f, 1.0f),
//            (reqString) -> reqString.startsWith("forest")
//    );

    private final String title;
    private final Item icon;
    private final Vector4f color;
    private final Predicate<String> satisfies;

    ProfessionsSelectionType(String title, Item icon, Vector4f color, Predicate<String> satisfies) {
        this.title = title;
        this.icon = icon;
        this.color = color;
        this.satisfies = satisfies;
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

    public boolean satisfies(String name) {
        return satisfies.test(name);
    }
}

