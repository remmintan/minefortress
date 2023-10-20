package org.minefortress.professions.hire;

import net.minecraft.item.Item;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireCost;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;
import org.minefortress.fortress.resources.ItemInfo;

import java.io.Serializable;

public record HireCost(int itemId, int amount) implements Serializable, IHireCost {

    @Override
    public IItemInfo toItemInfo() {
        return new ItemInfo(Item.byRawId(itemId), amount);
    }

    static IHireCost fromItemInfo(ItemInfo info) {
        return new HireCost(Item.getRawId(info.item()), info.amount());
    }

}
