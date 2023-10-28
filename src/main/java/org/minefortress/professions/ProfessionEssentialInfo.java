package org.minefortress.professions;

import net.remmintan.mods.minefortress.core.dtos.professions.IProfessionEssentialInfo;

public record ProfessionEssentialInfo (String id, int amount) implements IProfessionEssentialInfo {}
