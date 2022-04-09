package org.minefortress.professions;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import org.apache.logging.log4j.util.Strings;
import org.minefortress.fortress.AbstractFortressManager;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Map.entry;

public abstract class ProfessionManager {

    public final Map<String, Profession> professions = Map.ofEntries(
            entry(
                    "colonist",
                    new Profession(
                            "Colonist",
                            Items.PLAYER_HEAD,
                            "Can do any type of work, but is not very good at it.\nCan use only wooden tools.\nCan't fall tall trees\nCan't provide any food.",
                            ""
                    )
            ),
            // dig
            entry(
                    "miner1",
                    new Profession(
                            "Miner - LVL1",
                            Items.STONE_PICKAXE,
                            "Can work in mine and quarry.\nCan use stone shovel and pickaxe.",
                            "Build 'Wooden Miner's house' to unlock",
                            "miner_wooden"
                    )
            ),
            entry(
                    "miner2",
                    new Profession(
                            "Miner - LVL2",
                            Items.IRON_PICKAXE,
                            "Can work in mine and quarry.\nCan use iron shovel and pickaxe.",
                            "Build 'Stone Miner's house' to unlock",
                            "miner_stone"
                    )
            ),
            entry(
                    "miner3",
                    new Profession(
                            "Miner - LVL3",
                            Items.DIAMOND_PICKAXE,
                            "Can work in mine and quarry.\nCan use diamond shovel and pickaxe.",
                            "Build 'Miners' guild house' to unlock",
                            "miners_guild"
                    )
            ),
            // fall trees
            entry(
                    "lumberjack1",
                    new Profession(
                            "Lumberjack - LVL1",
                            Items.STONE_AXE,
                            "Can fall tall trees.\nCan use stone axe.\nCollects saplings.",
                            "Build 'Wooden Lumberjack's house' to unlock",
                            "lumberjack_wooden"
                    )
            ),
            entry(
                    "lumberjack2",
                    new Profession(
                            "Lumberjack - LVL2",
                            Items.IRON_AXE,
                            "Can fall tall trees.\nCan use iron axe.\nCan plant saplings.",
                            "Build 'Stone Lumberjack's house' to unlock",
                            "lumberjack_stone"
                    )
            ),
            entry(
                    "lumberjack3",
                    new Profession(
                            "Lumberjack - LVL3",
                            Items.DIAMOND_AXE,
                            "Can fall tall trees.\nCan use diamond axe.\nCan plant saplings.",
                            "Build 'Lumberjack's guild house' to unlock",
                            "lumberjack_guild"
                    )
            ),
            // food / defence
            entry(
                    "forester",
                    new Profession(
                            "Forester",
                            Items.APPLE,
                            "Hunts animals\nCollects seeds\nGet's and cooks on fire some basic food.\nCan collect and plant saplings.",
//                            "Build 'Forester's house' to unlock",
                            "Will be available in 1.5-alpha",
                            "forester"
                    )
            ),
            entry(
                    "hunter",
                    new Profession(
                            "Hunter",
                            Items.BOW,
                            "Can use bow.\nDefends village from monsters.\nHunts monsters.\nCan work at night",
                            "Build 'Shooting gallery' to unlock",
                            "shooting_gallery"
                    )
            ),
            entry(
                    "fisherman",
                    new Profession(
                            "Fisherman",
                            Items.FISHING_ROD,
                            "Catches fish in ponds\nCooks fish",
                            "Build 'Fishing hut' to unlock",
                            "fisher"
                    )
            ),
            entry(
                    "farmer",
                    new Profession(
                            "Farmer",
                            Items.WHEAT,
                            "Plants any kind of seeds including wheat, watermelon and pumpkin",
                            "Build 'Farm' to unlock",
                            "farmer"
                    )
            ),
            entry(
                    "baker",
                    new Profession(
                            "Baker",
                            Items.BREAD,
                            "Bakes bread, cakes and other food",
                            "Build 'Bakery' to unlock",
                            "backer"
                    )
            ),
            entry(
                    "shepherd",
                    new Profession(
                            "Shepherd",
                            Items.CARROT_ON_A_STICK,
                            "Brings pigs, sheeps and cows to the village.\nProvides milks, wool and meat",
//                            "Build 'Animal Pen' to unlock",
                            "Will be available in 1.5-alpha",
                            "_"
                    )
            ),
            entry(
                    "stableman",
                    new Profession(
                            "Stableman",
                            Items.LEAD,
                            "",
                            "Will be available in 1.5-alpha",
                            "_"
                    )
            ),
            entry(
                    "butcher",
                    new Profession(
                            "Butcher",
                            Items.BEEF,
                            "",
                            "Will be available in 1.5-alpha",
                            "_"
                    )
            ),
            entry(
                    "cook",
                    new Profession(
                            "Cook",
                            Items.COOKED_BEEF,
                            "",
                            "Will be available in 1.5-alpha",
                            "_"
                    )
            ),
            // craft / smith
            entry(
                    "crafter",
                    new Profession(
                            "Crafter",
                            Items.CRAFTING_TABLE,
                            "Can craft any item that doesn't need smelting.\nCan't use redstone or nether blocks/items",
                            "Build crafting table to unlock",
                            "_"
                    ).setBlockRequirement(Blocks.CRAFTING_TABLE)
            ),
            entry(
                    "leather_worker1",
                    new Profession(
                            "Leather Worker - LVL1",
                            Items.LEATHER,
                            "",
                            "Will be available in future releases",
                            "_"
                    )
            ),
            entry(
                    "leather_worker2",
                    new Profession(
                            "Leather Worker - LVL2",
                            Items.LEATHER_HORSE_ARMOR,
                            "",
                            "Will be available in future releases",
                            "_"
                    )
            ),
            entry(
                    "blacksmith",
                    new Profession(
                            "Blacksmith",
                            Items.IRON_INGOT,
                            "",
                            "Will be available in future releases",
                            "_"
                    )
            ),
            entry(
                    "armorer",
                    new Profession(
                            "Armorer",
                            Items.IRON_CHESTPLATE,
                            "",
                            "Will be available in future releases",
                            "_"
                    )
            ),
            entry(
                    "weaver",
                    new Profession(
                            "Weaver",
                            Items.STRING,
                            "",
                            "Will be available in future releases",
                            "_"
                    )
            ),
            entry(
                    "tailor",
                    new Profession(
                            "Tailor",
                            Items.WHITE_BANNER,
                            "",
                            "Will be available in future releases",
                            "_"
                    )
            ),
            // combat
            entry(
                    "warrior1",
                    new Profession(
                            "Warrior - LVL1",
                            Items.STONE_SWORD,
                            "",
                            "Will be available in future releases",
                            "_"
                    )
            ),
            entry(
                    "warrior2",
                    new Profession(
                            "Warrior - LVL2",
                            Items.IRON_SWORD,
                            "",
                            "Will be available in future releases",
                            "_"
                    )
            ),
            entry(
                    "archer1",
                    new Profession(
                            "Archer",
                            Items.BOW,
                            "Will be available in future releases",
                            "",
                            "_"
                    )
            ),
            entry(
                    "archer2",
                    new Profession(
                            "Archer - LVL2",
                            Items.CROSSBOW,
                            "Will be available in future releases",
                            "",
                            "_"
                    )
            ),
            entry(
                    "knight1",
                    new Profession(
                            "Knight - LVL1",
                            Items.IRON_HORSE_ARMOR,
                            "Will be available in future releases",
                            "",
                            "_"
                    )
            ),
            entry(
                    "knight2",
                    new Profession(
                            "Knight - LVL2",
                            Items.DIAMOND_HORSE_ARMOR,
                            "Will be available in future releases",
                            "",
                            "_"
                    )
            )
    );

    private final Profession root;
    protected final Supplier<AbstractFortressManager> fortressManagerSupplier;

    public ProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
        this.root = this.createProfessionTree();
        this.fortressManagerSupplier = fortressManagerSupplier;
    }

    public Profession getRootProfession() {
        return this.root;
    }

    public boolean isRequirementsFulfilled(Profession profession) {
        if(fortressManagerSupplier.get().isCreative())
            return true;

        final String buildingRequirement = profession.getBuildingRequirement();
        if(Objects.isNull(buildingRequirement) || Strings.isBlank(buildingRequirement)) {
            return true;
        }

        final Profession parent = profession.getParent();
        if(Objects.nonNull(parent)) {
            final boolean parentUnlocked = this.isRequirementsFulfilled(parent);
            if(!parentUnlocked) {
                return false;
            }
        }

        final AbstractFortressManager fortressManager = fortressManagerSupplier.get();
        boolean satisfied = fortressManager.hasRequiredBuilding(buildingRequirement);
        final Block blockRequirement = profession.getBlockRequirement();
        if(Objects.nonNull(blockRequirement)) {
            satisfied = satisfied || fortressManager.hasRequiredBlock(blockRequirement);
        }
        return satisfied;
    }

    public Profession getProfession(String name) {
        return professions.get(name);
    }

    /**
     * colonist -> miner1, lumberjack1, forester, crafter
     * miner1 -> miner2
     * lumberjack1 -> lumberjack2
     * forester -> hunter, fisherman, farmer
     * crafter -> leather_worker1
     * miner2 -> miner3
     * lumberjack2 -> lumberjack3
     * hunter -> warrior1, archer1, knight1
     * farmer -> baker, shepherd
     * leather_worker1 -> leather_worker2, blacksmith, weaver
     * warrior1 -> warrior2
     * archer1 -> archer2
     * knight1 -> knight2
     * shepherd -> stableman, butcher
     * blacksmith -> armorer
     * weaver -> tailor
     * butcher -> cook
     */
    private Profession createProfessionTree() {
        Profession colonist = getProfession("colonist");

        // colonist -> miner1, lumberjack1, forester, crafter
        Profession miner1 = getProfession("miner1");
        Profession lumberjack1 = getProfession("lumberjack1");
        Profession forester = getProfession("forester");
        Profession crafter = getProfession("crafter");
        addChildren(colonist, miner1, lumberjack1, forester, crafter);

        // miner1 -> miner2
        Profession miner2 = getProfession("miner2");
        addChildren(miner1, miner2);

        // lumberjack1 -> lumberjack2
        Profession lumberjack2 = getProfession("lumberjack2");
        addChildren(lumberjack1, lumberjack2);

        // forester -> hunter, fisherman, farmer
        Profession hunter = getProfession("hunter");
        Profession fisherman = getProfession("fisherman");
        Profession farmer = getProfession("farmer");
        addChildren(forester, hunter, fisherman, farmer);

        // crafter -> leather_worker1
        Profession leather_worker1 = getProfession("leather_worker1");
        addChildren(crafter, leather_worker1);

        // miner2 -> miner3
        Profession miner3 = getProfession("miner3");
        addChildren(miner2, miner3);

        // lumberjack2 -> lumberjack3
        Profession lumberjack3 = getProfession("lumberjack3");
        addChildren(lumberjack2, lumberjack3);

        // hunter -> warrior1, archer1, knight1
        Profession warrior1 = getProfession("warrior1");
        Profession archer1 = getProfession("archer1");
        Profession knight1 = getProfession("knight1");
        addChildren(hunter, warrior1, archer1, knight1);

        // farmer -> baker, shepherd
        Profession baker = getProfession("baker");
        Profession shepherd = getProfession("shepherd");
        addChildren(farmer, baker, shepherd);

        // leather_worker1 -> leather_worker2, blacksmith, weaver
        Profession leather_worker2 = getProfession("leather_worker2");
        Profession blacksmith = getProfession("blacksmith");
        Profession weaver = getProfession("weaver");
        addChildren(leather_worker1, leather_worker2, blacksmith, weaver);

        // warrior1 -> warrior2
        Profession warrior2 = getProfession("warrior2");
        addChildren(warrior1, warrior2);

        // archer1 -> archer2
        Profession archer2 = getProfession("archer2");
        addChildren(archer1, archer2);

        // knight1 -> knight2
        Profession knight2 = getProfession("knight2");
        addChildren(knight1, knight2);

        // shepherd -> stableman, butcher
        Profession stableman = getProfession("stableman");
        Profession butcher = getProfession("butcher");
        addChildren(shepherd, stableman, butcher);

        // blacksmith -> armorer
        Profession armorer = getProfession("armorer");
        addChildren(blacksmith, armorer);

        // weaver -> tailor
        Profession tailor = getProfession("tailor");
        addChildren(weaver, tailor);

        // butcher -> cook
        Profession cook = getProfession("cook");
        addChildren(butcher, cook);

        return colonist;
    }

    private void addChildren(Profession parent, Profession... children) {
        for (Profession child : children) {
            parent.addChild(child);
            child.setParent(parent);
        }
    }

    public int getFreeColonists() {
        final int totalColonists = fortressManagerSupplier.get().getTotalColonistsCount();
        final int totalWorkers = professions.values().stream().mapToInt(Profession::getAmount).sum();
        return totalColonists - totalWorkers;
    }

    public Optional<String> findIdFromProfession(Profession profession) {
        return professions.entrySet().stream().filter(entry -> entry.getValue() == profession).map(Map.Entry::getKey).findFirst();
    }

    public abstract void increaseAmount(String professionId);
    public abstract void decreaseAmount(String professionId);
}
