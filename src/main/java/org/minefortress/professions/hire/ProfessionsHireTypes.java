package org.minefortress.professions.hire;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public enum ProfessionsHireTypes {

    WARRIORS("Hire warriors", Set.of("warrior1", "warrior2", "archer1", "archer2")),
    MINERS("Hire miners", Set.of("miner1", "miner2", "miner3")),
    LUMBERJACKS("Hire lumberjacks", Set.of("lumberjack1", "lumberjack2", "lumberjack3"));

    private final Set<String> ids;
    private final String screenName;

    ProfessionsHireTypes(String screenName, Set<String> ids) {
        this.screenName = screenName;
        this.ids = Collections.unmodifiableSet(ids);
    }

    private boolean contains(String id) {
        return ids.contains(id);
    }

    public List<String> getIds() {
        return List.copyOf(ids);
    }

    public String getScreenName() {
        return screenName;
    }

    public static Optional<ProfessionsHireTypes> getHireType(String id) {
        for (ProfessionsHireTypes type : values()) {
            if (type.contains(id)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
