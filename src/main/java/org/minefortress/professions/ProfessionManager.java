package org.minefortress.professions;

import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.remmintan.mods.minefortress.core.interfaces.IFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.professions.CountProfessionals;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfession;
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfessionsManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.ProfessionResearchState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public abstract class ProfessionManager implements IProfessionsManager {

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

    private IProfession root;
    private Map<String, IProfession> professions = Collections.emptyMap();

    public ProfessionManager() {
    }

    protected abstract IFortressManager getFortressManager();

    @Override
    public IProfession getRootProfession() {
        return this.root;
    }

    @Override
    public final ProfessionResearchState isRequirementsFulfilled(IProfession profession, CountProfessionals countProfessionals) {
        final var requirementType = profession.getRequirementType();
        if (requirementType == null) {
            return ProfessionResearchState.UNLOCKED;
        }

        final IProfession parent = profession.getParent();
        if(Objects.nonNull(parent)) {
            final var parentState = this.isRequirementsFulfilled(parent, CountProfessionals.DONT_COUNT);
            if(parentState != ProfessionResearchState.UNLOCKED) {
                return ProfessionResearchState.LOCKED_PARENT;
            }
        }
        final var fortressManager = getFortressManager();

        if (isCreativeFortress()) {
            return ProfessionResearchState.UNLOCKED;
        }

        var minRequirementCount = 0;
        if(countProfessionals == CountProfessionals.INCREASE) {
            minRequirementCount = profession.getAmount();
        }

        boolean satisfied = fortressManager.hasRequiredBuilding(requirementType, profession.getRequirementLevel(), minRequirementCount);
        return satisfied ? ProfessionResearchState.UNLOCKED : ProfessionResearchState.LOCKED_SELF;
    }

    abstract boolean isCreativeFortress();

    @Override
    public IProfession getProfession(String id) {
        return getProfessions().get(id);
    }

    @Override
    public List<IProfession> getProfessionsByType(ProfessionType type) {
        if (type == null) return List.of();
        return getProfessions()
                .values()
                .stream()
                .filter(it -> it.getRequirementType() == type)
                .sorted(Comparator.comparing(IProfession::getRequirementLevel))
                .toList();
    }

    @Unmodifiable
    @NotNull
    protected Map<String, IProfession> getProfessions(){
        return this.professions;
    }

    protected void setProfessions(Map<String, IProfession> professions) {
        this.professions = Collections.unmodifiableMap(professions);
    }

    @Override
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

    private void readChildren(JsonReader reader, IProfession parent) throws IOException {
        final var childrenProfession = new ArrayList<IProfession>();
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

    private void addChildren(IProfession parent, List<IProfession> children) {
        for (IProfession child : children) {
            parent.addChild(child);
            child.setParent(parent);
        }
    }

    @Override
    public int getFreeColonists() {
        final var abstractFortressManager = getFortressManager();
        final int totalColonists = abstractFortressManager.getTotalColonistsCount();
        final int reservedColonists = abstractFortressManager.getReservedPawnsCount();
        final int totalWorkers = getProfessions().values().stream().mapToInt(IProfession::getAmount).sum();
        return totalColonists - totalWorkers - reservedColonists;
    }
}
