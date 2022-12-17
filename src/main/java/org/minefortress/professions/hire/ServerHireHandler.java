package org.minefortress.professions.hire;

import java.util.List;
import java.util.Map;

public class ServerHireHandler {

    private final List<String> professions;

    public ServerHireHandler(List<String> professions) {
        this.professions = professions;
    }

    public Map<String, HireInfo> getProfessions() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
