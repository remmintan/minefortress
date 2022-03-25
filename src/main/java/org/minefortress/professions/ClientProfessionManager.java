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
            entry("stableman", new Profession("Stableman", Items.LEAD)),
            entry("shepherd", new Profession("Shepherd", Items.CARROT_ON_A_STICK)),
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

    public AdvancementObtainedStatus getStatus(Profession profession) {
        return AdvancementObtainedStatus.OBTAINED;
    }

    public Profession getProfession(String name) {
        return professions.get(name);
    }

}
