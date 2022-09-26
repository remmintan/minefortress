package org.minefortress.professions;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.logging.log4j.util.Strings;
import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.*;
import java.util.function.Supplier;

import static java.util.Map.entry;

public abstract class ProfessionManager {

    public static final List<Item> FORESTER_ITEMS = Arrays.asList(
            Items.BEETROOT_SEEDS,
            Items.CARROT,
            Items.POTATO,
            Items.WHEAT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.MELON_SEEDS,
            Items.APPLE
    );

    public static final List<Item> FISHERMAN_ITEMS = Arrays.asList(
            Items.COD,
            Items.SALMON,
            Items.TROPICAL_FISH,
            Items.PUFFERFISH,
            Items.INK_SAC,
            Items.GLOW_INK_SAC
    );

    private final Profession root;
    private final Map<String, Profession> professions = new HashMap<>();
    protected final Supplier<AbstractFortressManager> fortressManagerSupplier;

    public ProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
        this.root = this.createProfessionTree();
        this.fortressManagerSupplier = fortressManagerSupplier;
    }

    public Profession getRootProfession() {
        return this.root;
    }

    public boolean isRequirementsFulfilled(Profession profession) {
        return isRequirementsFulfilled(profession, false);
    }

    public boolean isRequirementsFulfilled(Profession profession, boolean countProfessionals) {
        if(fortressManagerSupplier.get().isCreative())
            return true;

        final String buildingRequirement = profession.getBuildingRequirement();
        if(Objects.isNull(buildingRequirement) || Strings.isBlank(buildingRequirement)) {
            return true;
        }

        final Profession parent = profession.getParent();
        if(Objects.nonNull(parent)) {
            final boolean parentUnlocked = this.isRequirementsFulfilled(parent, false);
            if(!parentUnlocked) {
                return false;
            }
        }

        final AbstractFortressManager fortressManager = fortressManagerSupplier.get();
        final var minRequirementCount = countProfessionals ? profession.getAmount() : 0;
        boolean satisfied = fortressManager.hasRequiredBuilding(buildingRequirement, minRequirementCount);
        final Profession.BlockRequirement blockRequirement = profession.getBlockRequirement();
        if(Objects.nonNull(blockRequirement)) {
            satisfied = satisfied || fortressManager.hasRequiredBlock(blockRequirement.block(), blockRequirement.blueprint(), minRequirementCount);
        }

        final var itemsRequirement = profession.getItemsRequirement();
        if(countProfessionals && Objects.nonNull(itemsRequirement)) {
            final var hasItems = fortressManager.getResourceManager().hasItems(itemsRequirement);
            satisfied = satisfied && hasItems;
        }

        return satisfied;
    }

    public Profession getProfession(String name) {
        return getProfessions().get(name);
    }

    protected Map<String, Profession> getProfessions(){
        return this.professions;
    }

    public boolean hasProfession(String name) {
        return getProfessions().containsKey(name) && getProfessions().get(name).getAmount() > 0;
    }

    /**
     * colonist -> miner1, lumberjack1, forester, crafter
     * miner1 -> miner2
     * lumberjack1 -> lumberjack2
     * forester -> hunter, fisherman, farmer, warrior1
     * crafter -> blacksmith
     * miner2 -> miner3
     * lumberjack2 -> lumberjack3
     * hunter -> archer1, knight1
     * farmer -> baker, shepherd
     * blacksmith -> leather_worker1, weaver
     * leather_worker1 -> leather_worker2
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

        // forester -> warrior1, hunter, fisherman, farmer
        Profession warrior1 = getProfession("warrior1");
        Profession hunter = getProfession("hunter");
        Profession fisherman = getProfession("fisherman");
        Profession farmer = getProfession("farmer");
        addChildren(forester, warrior1, hunter, fisherman, farmer);

        // crafter -> blacksmith
        Profession blacksmith = getProfession("blacksmith");
        addChildren(crafter, blacksmith);

        // miner2 -> miner3
        Profession miner3 = getProfession("miner3");
        addChildren(miner2, miner3);

        // lumberjack2 -> lumberjack3
        Profession lumberjack3 = getProfession("lumberjack3");
        addChildren(lumberjack2, lumberjack3);

        // hunter -> archer1, knight1
        Profession knight1 = getProfession("knight1");
        addChildren(hunter, knight1);

        // farmer -> baker, shepherd
        Profession baker = getProfession("baker");
        Profession shepherd = getProfession("shepherd");
        addChildren(farmer, baker, shepherd);

        // blacksmith -> leather_worker1, weaver
        Profession leather_worker1 = getProfession("leather_worker1");
        Profession weaver = getProfession("weaver");
        addChildren(blacksmith, leather_worker1, weaver);

        // leather_worker1 -> leather_worker2
        Profession leather_worker2 = getProfession("leather_worker2");
        addChildren(leather_worker1, leather_worker2);

        // warrior1 -> warrior2
        Profession warrior2 = getProfession("warrior2");
        Profession archer1 = getProfession("archer1");
        addChildren(warrior1, warrior2, archer1);

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
        final int totalWorkers = getProfessions().values().stream().mapToInt(Profession::getAmount).sum();
        return totalColonists - totalWorkers;
    }

    public Optional<String> findIdFromProfession(Profession profession) {
        return getProfessions().entrySet().stream().filter(entry -> entry.getValue() == profession).map(Map.Entry::getKey).findFirst();
    }

    public abstract void increaseAmount(String professionId);
    public abstract void decreaseAmount(String professionId);
}
