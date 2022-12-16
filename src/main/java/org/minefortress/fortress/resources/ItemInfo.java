package org.minefortress.fortress.resources;

import net.minecraft.item.Item;

import java.io.Serializable;

public record ItemInfo(Item item, int amount) implements Serializable {}
