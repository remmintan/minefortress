package org.minefortress.professions;

import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.apache.logging.log4j.util.Strings;
import org.minefortress.fortress.AbstractFortressManager;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.Supplier;

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

    private Profession root;
    private final Map<String, Profession> professions = new HashMap<>();
    protected final Supplier<AbstractFortressManager> fortressManagerSupplier;

    public ProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
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

    public Profession getProfession(String id) {
        return getProfessions().get(id);
    }

    protected Map<String, Profession> getProfessions(){
        return this.professions;
    }

    public boolean hasProfession(String name) {
        return getProfessions().containsKey(name) && getProfessions().get(name).getAmount() > 0;
    }

    protected void createProfessionTree(String treeJson) {
        try (
            var sr = new StringReader(treeJson);
            var jsonReader = new JsonReader(sr)
        ) {
            jsonReader.beginObject();

            final var rootProfessionName = jsonReader.nextName();
            this.root = getProfession(rootProfessionName);
            readChildren(jsonReader, this.root);

            if(jsonReader.hasNext()) {
                throw new IllegalStateException("Expected end of object, but found more.");
            }

            jsonReader.endObject();
        } catch (IOException exception) {
            throw new RuntimeException("cannot create profession tree", exception);
        }
    }

    private void readChildren(JsonReader reader, Profession parent) throws IOException {
        final var childrenProfession = new ArrayList<Profession>();
        reader.beginObject();
        while (reader.hasNext()) {
            final var childName = reader.nextName();
            final var childProfession = getProfession(childName);
            readChildren(reader, childProfession);
            childrenProfession.add(childProfession);
        }
        reader.endObject();
        if(!childrenProfession.isEmpty()) {
            addChildren(parent, childrenProfession);
        }
    }

    private void addChildren(Profession parent, List<Profession> children) {
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
