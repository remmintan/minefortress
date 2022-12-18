package org.minefortress.professions.hire;

import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.professions.ServerProfessionManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerHireHandler {

    private final List<String> professions;
    private final ServerProfessionManager professionManager;

    private final Map<String, List<HireRequest>> hireRequests = new HashMap<>();

    public ServerHireHandler(List<String> professions, ServerProfessionManager professionManager) {
        this.professions = Collections.unmodifiableList(professions);
        this.professionManager = professionManager;
    }

    public Map<String, HireInfo> getProfessions() {
        return getUnlockedProfessions().stream()
                .map(it ->
                        new HireInfo(it,
                        0,
                        getCost(it))
                )
                .collect(Collectors.toMap(HireInfo::professionId, it -> it));
    }

    public void hire(String professionId) {
        final var hireRequest = new HireRequest(professionId);
        hireRequests.computeIfAbsent(professionId, k -> Collections.emptyList()).add(hireRequest);
    }

    private List<ItemInfo> getCost(String it) {
        return professionManager.getProfession(it).getItemsRequirement();
    }

    public void tick() {
        hireRequests.entrySet()
                .stream()
                .filter(it -> this.professionUnlocked(it.getKey()))
                .flatMap(it -> it.getValue().stream())
                .forEach(it -> {
                    it.tick();
                    if(it.isDone()){
                        professionManager.increaseAmount(it.getProfessionId());
                    }
                });

        for (Map.Entry<String, List<HireRequest>> entry : hireRequests.entrySet()) {
            entry.getValue().removeIf(HireRequest::isDone);
        }
    }

    private List<String> getUnlockedProfessions() {
        return professions.stream().filter(this::professionUnlocked).toList();
    }

    private boolean professionUnlocked(String it) {
        final var profession = professionManager.getProfession(it);
        return professionManager.isRequirementsFulfilled(profession, true, false);
    }

    private static class HireRequest {
        private final String professionId;
        private int progress; // 0 - 100

        public HireRequest(String professionId) {
            this.professionId = professionId;
        }

        // get id
        String getProfessionId() {
            return professionId;
        }

        void tick() {
            progress++;
        }

        boolean isDone() {
            return progress >= 100;
        }
    }

}
