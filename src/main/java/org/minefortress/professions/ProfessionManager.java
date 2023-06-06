package org.minefortress.professions;

import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
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
    private Map<String, Profession> professions = Collections.emptyMap();
    protected final Supplier<AbstractFortressManager> fortressManagerSupplier;

    public ProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
        this.fortressManagerSupplier = fortressManagerSupplier;
    }

    public Profession getRootProfession() {
        return this.root;
    }

    public final boolean isRequirementsFulfilled(Profession profession, CountProfessionals countProfessionals, boolean countItems) {
        final String buildingRequirement = profession.getBuildingRequirement();
        if(Objects.isNull(buildingRequirement) || Strings.isBlank(buildingRequirement)) {
            return true;
        }

        final Profession parent = profession.getParent();
        if(Objects.nonNull(parent)) {
            final boolean parentUnlocked = this.isRequirementsFulfilled(parent, CountProfessionals.DONT_COUNT, false);
            if(!parentUnlocked) {
                return false;
            }
        }

        final var disabled = "_".equals(buildingRequirement) &&
                Optional.ofNullable(profession.getBlockRequirement()).map(it -> it.block() == null).orElse(true);
        if(fortressManagerSupplier.get().isCreative() && !disabled) {
            return true;
        }

        final AbstractFortressManager fortressManager = fortressManagerSupplier.get();
        var minRequirementCount = 0;
        if(countProfessionals == CountProfessionals.INCREASE) {
            minRequirementCount = profession.getAmount();
        }

        boolean satisfied = fortressManager.hasRequiredBuilding(buildingRequirement, minRequirementCount);
        final Profession.BlockRequirement blockRequirement = profession.getBlockRequirement();
        if(Objects.nonNull(blockRequirement)) {
            satisfied = satisfied || fortressManager.hasRequiredBlock(blockRequirement.block(), blockRequirement.blueprint(), minRequirementCount);
        }

        if(countItems) {
            final var itemsRequirement = profession.getItemsRequirement();
            if(countProfessionals != CountProfessionals.DONT_COUNT && Objects.nonNull(itemsRequirement)) {
                final var hasItems = fortressManager.getResourceManager().hasItems(itemsRequirement);
                satisfied = satisfied && hasItems;
            }
        }

        return satisfied;
    }

    public Profession getProfession(String id) {
        return getProfessions().get(id);
    }

    public Optional<Profession> getByBuildingRequirement(String requirement) {
        return getProfessions().values()
                .stream()
                .filter(
                    profession -> Optional
                        .ofNullable(profession.getBuildingRequirement())
                        .orElse("!!!!some-invalid-requirement!!!!")
                        .equals(requirement)
                )
                .findFirst();
    }

    @Unmodifiable
    @NotNull
    protected Map<String, Profession> getProfessions(){
        return this.professions;
    }

    protected void setProfessions(Map<String, Profession> professions) {
        this.professions = Collections.unmodifiableMap(professions);
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
        final var abstractFortressManager = fortressManagerSupplier.get();
        final int totalColonists = abstractFortressManager.getTotalColonistsCount();
        final int reservedColonists = abstractFortressManager.getReservedPawnsCount();
        final int totalWorkers = getProfessions().values().stream().mapToInt(Profession::getAmount).sum();
        return totalColonists - totalWorkers - reservedColonists;
    }

    public Optional<String> findIdFromProfession(Profession profession) {
        return getProfessions().entrySet().stream().filter(entry -> entry.getValue() == profession).map(Map.Entry::getKey).findFirst();
    }

    public abstract void increaseAmount(String professionId, boolean alreadyCharged);
    public abstract void decreaseAmount(String professionId);

    public enum CountProfessionals {
        INCREASE,
        DONT_COUNT
    }
}
