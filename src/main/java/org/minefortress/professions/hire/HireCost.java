package org.minefortress.professions.hire;

import net.minecraft.item.Item;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireCost;

import java.io.Serializable;

public record HireCost(int itemId, int amount) implements Serializable, IHireCost {

    static IHireCost fromItemInfo(ItemInfo info) {
        return new HireCost(Item.getRawId(info.item()), info.amount());
    }

    @Override
    public ItemInfo toItemInfo() {
        return new ItemInfo(Item.byRawId(itemId), amount);
    }

}
