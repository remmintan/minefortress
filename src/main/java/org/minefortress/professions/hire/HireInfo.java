package org.minefortress.professions.hire;

import java.io.Serializable;
import java.util.List;

public record HireInfo(
        String professionId,
        int hireProgress,
        List<HireCost> cost
) implements Serializable {}