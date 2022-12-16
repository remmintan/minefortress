package org.minefortress.professions.hire;

import org.minefortress.fortress.resources.ItemInfo;

import java.io.Serializable;
import java.util.List;

public record HireInfo(
        String professionId,
        int hireProgress,
        List<ItemInfo> cost
) implements Serializable {}