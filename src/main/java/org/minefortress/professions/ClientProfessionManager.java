package org.minefortress.professions;

import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.item.Items;

import java.util.Map;

import static java.util.Map.entry;

public class ClientProfessionManager {

    public final Map<String, Profession> professions = Map.ofEntries(
            entry("colonist", new Profession("Colonist", Items.PLAYER_HEAD)),
            // dig
            entry("miner1", new Profession("Miner - LVL1", Items.STONE_PICKAXE)),
            entry("miner2", new Profession("Miner - LVL2", Items.IRON_PICKAXE)),
            entry("miner3", new Profession("Miner - LVL3", Items.DIAMOND_PICKAXE)),
            // fall trees
            entry("lumberjack1", new Profession("Lumberjack - LVL1", Items.STONE_AXE)),
            entry("lumberjack2", new Profession("Lumberjack - LVL2", Items.IRON_AXE)),
            entry("lumberjack3", new Profession("Lumberjack - LVL3", Items.DIAMOND_AXE)),
            // food / defence
            entry("forester", new Profession("Forester", Items.APPLE)),
            entry("hunter", new Profession("Hunter", Items.BOW)),
            entry("fisherman", new Profession("Fisherman", Items.FISHING_ROD)),
            entry("farmer", new Profession("Farmer", Items.WHEAT)),
            entry("baker", new Profession("Baker", Items.BREAD)),
            entry("shepherd", new Profession("Shepherd", Items.CARROT_ON_A_STICK)),
            entry("stableman", new Profession("Stableman", Items.LEAD)),
            entry("butcher", new Profession("Butcher", Items.BEEF)),
            entry("cook", new Profession("Cook", Items.COOKED_BEEF)),
            // craft / smith
            entry("crafter", new Profession("Crafter", Items.CRAFTING_TABLE)),
            entry("leather_worker1", new Profession("Leather Worker - LVL1", Items.LEATHER)),
            entry("leather_worker2", new Profession("Leather Worker - LVL2", Items.LEATHER_HORSE_ARMOR)),
            entry("blacksmith", new Profession("Blacksmith", Items.IRON_INGOT)),
            entry("armorer", new Profession("Armorer", Items.IRON_CHESTPLATE)),
            entry("weaver", new Profession("Weaver", Items.STRING)),
            entry("tailor", new Profession("Tailor", Items.WHITE_BANNER)),
            // combat
            entry("warrior1", new Profession("Warrior - LVL1", Items.STONE_SWORD)),
            entry("warrior2", new Profession("Warrior - LVL2", Items.IRON_SWORD)),
            entry("archer1", new Profession("Archer", Items.BOW)),
            entry("archer2", new Profession("Archer - LVL2", Items.CROSSBOW)),
            entry("knight1", new Profession("Knight - LVL1", Items.IRON_HORSE_ARMOR)),
            entry("knight2", new Profession("Knight - LVL2", Items.DIAMOND_HORSE_ARMOR))
    );

    private final Profession root;

    public ClientProfessionManager() {
        this.root = this.createProfessionTree();
    }

    public Profession getRootProfession() {
        return this.root;
    }

    public static AdvancementObtainedStatus getStatus(Profession profession) {
        return AdvancementObtainedStatus.OBTAINED;
    }

    private Profession getProfession(String name) {
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

}
