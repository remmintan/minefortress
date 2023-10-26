package org.minefortress.professions.hire;

import net.remmintan.mods.minefortress.core.interfaces.professions.CountProfessionals;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireCost;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;
import net.remmintan.mods.minefortress.core.interfaces.professions.ProfessionResearchState;
import org.jetbrains.annotations.NotNull;
import org.minefortress.professions.ServerProfessionManager;

import java.util.*;
import java.util.stream.Collectors;

public class ServerHireHandler {

    private final List<String> professions;
    private final ServerProfessionManager professionManager;

    private final Map<String, Queue<HireRequest>> hireRequests = new HashMap<>();

    public ServerHireHandler(List<String> professions, ServerProfessionManager professionManager) {
        this.professions = Collections.unmodifiableList(professions);
        this.professionManager = professionManager;
    }

    public Map<String, IHireInfo> getProfessions() {
        return getUnlockedProfessions()
                .stream()
                .map(it ->
                        {
                            final var hireRequestsQueue = getHireRequestsQueue(it);
                            return new HireInfo(
                                it,
                                Optional.ofNullable(hireRequestsQueue.peek())
                                        .map(HireRequest::getProgress)
                                        .orElse(0),
                                hireRequestsQueue.size(),
                                getCost(it)
                            );
                        }
                )
                .collect(Collectors.toMap(IHireInfo::professionId, it -> it));
    }

    public void hire(String professionId) {
        final var hireRequest = new HireRequest(professionId);
        getHireRequestsQueue(professionId).add(hireRequest);
    }

    @NotNull
    private Queue<HireRequest> getHireRequestsQueue(String professionId) {
        return hireRequests.computeIfAbsent(professionId, k -> new ArrayDeque<>());
    }

    private List<IHireCost> getCost(String it) {
        return professionManager.getProfession(it)
                .getItemsRequirement()
                .stream()
                .map(HireCost::fromItemInfo)
                .toList();
    }

    public void tick() {
        hireRequests.entrySet()
                .stream()
                .filter(it -> this.professionUnlocked(it.getKey()))
                .map(it -> it.getValue().peek())
                .filter(Objects::nonNull)
                .forEach(HireRequest::tick);

        for (Map.Entry<String, Queue<HireRequest>> entry : hireRequests.entrySet()) {
            entry.getValue().removeIf(it -> {
                if(it.isDone()){
                    professionManager.increaseAmount(it.getProfessionId(), true);
                    return true;
                } else {
                    return false;
                }
            });
        }
    }

    private List<String> getUnlockedProfessions() {
        return professions.stream().filter(this::professionUnlocked).toList();
    }

    private boolean professionUnlocked(String it) {
        final var profession = professionManager.getProfession(it);
        return professionManager.isRequirementsFulfilled(profession, CountProfessionals.DONT_COUNT, false) == ProfessionResearchState.UNLOCKED;
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

        int getProgress() {
            return progress;
        }
    }

}
